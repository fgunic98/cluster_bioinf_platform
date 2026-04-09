# myCloudPlatformCluster

A self-hosted "platform" built around a Spring Boot REST API for managing
bioinformatics-style pipelines. The **real** purpose of the repo is to be a
hands-on DevOps / Kubernetes learning vehicle — favor idiomatic, "industry
standard" Kubernetes patterns over shortcuts, and explain *why* when suggesting
changes.

The user comes from an AWS background and is new to Kubernetes. When relevant,
map K8s concepts back to their AWS equivalents (EKS, RDS, ALB, Secrets Manager,
CloudWatch, etc.) to help concepts land.

## Repository layout

```
myCloudPlatformCluster/
├── pipeline/                    # Spring Boot 4 / Java 25 / Gradle backend
│   ├── src/main/java/com/orchestrator/pipeline/
│   │   ├── PipelineApplication.java
│   │   ├── controller/PipelineController.java   # GET/POST /api/pipelines
│   │   ├── model/Pipeline.java                  # JPA entity
│   │   ├── model/PipelineStatus.java            # enum
│   │   └── repository/PipelineRepository.java
│   ├── src/main/resources/
│   │   ├── application.properties               # JPA + actuator + prometheus
│   │   └── application.yaml
│   ├── docker/api-entrypoint.sh                 # waits for Vault Agent secrets
│   ├── Dockerfile                               # multi-stage gradle → temurin JRE
│   └── build.gradle
│
├── my-cloud-platform-fe/        # Vue 3 + Vite + TS frontend (scaffold only)
│
├── monitoring/                  # legacy compose-era prometheus.yml / promtail.yml
├── docker-compose-OLD-REFERENCE.yml   # legacy local stack, kept as reference
│
└── deploy/                      # the new Kubernetes world
    ├── kind/kind-config.yaml    # 1 control-plane + 2 workers, name: platform-local
    ├── helm/platform/           # umbrella Helm chart
    │   ├── Chart.yaml           # deps: platform-api, postgresql, kube-prometheus-stack, loki, alloy
    │   ├── values.yaml          # base values
    │   └── values-local.yaml    # kind overrides
    └── charts/platform-api/     # local subchart for the Spring Boot API
        ├── Chart.yaml
        ├── values.yaml
        └── templates/{deployment.yaml,service.yaml,_helpers.tpl}
```

## Architecture (current target state)

A single `helm install` of the umbrella chart `deploy/helm/platform` brings up,
inside a local **kind** cluster:

- **platform-api** (Spring Boot, ClusterIP :8080) → talks to **postgresql**
  (Bitnami chart). Exposes `/actuator/prometheus` for scraping and
  `/actuator/health` for probes.
- **kube-prometheus-stack** — Prometheus + Grafana + Alertmanager + CRDs.
  Scrapes via ServiceMonitors. Grafana is bundled here, not installed
  separately.
- **loki** — log store, deployed in `SingleBinary` mode for local simplicity.
- **alloy** — Grafana's log shipper, replaces the old `promtail` from the
  compose stack. Reads pod logs and forwards to Loki.

The `docker-compose-OLD-REFERENCE.yml` file represents the previous local setup
and is **kept on disk as a reference** for "which services need to exist." It
is not used at runtime — do not modify it as if it were live.

## Common commands

### Backend (pipeline/)
```bash
cd pipeline
./gradlew bootJar           # build the jar
./gradlew test              # run tests
docker build -t platform-api:local .
```

### Frontend (my-cloud-platform-fe/)
```bash
cd my-cloud-platform-fe
yarn install
yarn dev
yarn test:unit
yarn lint
```

### Cluster lifecycle (kind + helm)
```bash
# create / destroy the local cluster
kind create cluster --config deploy/kind/kind-config.yaml
kind delete cluster --name platform-local

# load a locally-built image into kind so the cluster can pull it
kind load docker-image platform-api:local --name platform-local

# pull chart dependencies before first install or after Chart.yaml changes
helm dependency update deploy/helm/platform

# install / upgrade the platform
helm upgrade --install platform deploy/helm/platform \
  -n platform --create-namespace \
  -f deploy/helm/platform/values-local.yaml

helm uninstall platform -n platform
```

### Inspecting the running cluster
```bash
kubectl get pods -n platform
kubectl logs -n platform <pod>
kubectl port-forward -n platform svc/platform-platform-api 8080:8080
kubectl port-forward -n platform svc/platform-kube-prometheus-stack-grafana 3000:80
```

## Conventions and things to know

- **Image naming inconsistency**: `deploy/charts/platform-api/values.yaml`
  defaults to image `simple-java-api`, but the umbrella `values.yaml` overrides
  it to `platform-api`. The umbrella is the source of truth at install time.
- **Probes**: liveness probe is enabled (`/actuator/health`); readiness probe
  is currently commented out in `deploy/charts/platform-api/values.yaml`. This
  is a known gap.
- **Secrets**: passwords are hardcoded as `admin/admin` in `values.yaml`. There
  is a `pipeline/docker/api-entrypoint.sh` that waits for Vault Agent to render
  `application.properties`, suggesting a planned Vault integration. Do not
  paper over this with K8s `Secret` objects without first checking with the
  user which direction they want to take.
- **No Ingress yet** — everything is `ClusterIP`, and Grafana is `NodePort` in
  `values-local.yaml`. Reaching services from the host means `kubectl
  port-forward`.
- **The frontend is not deployed via the chart yet.** It's a scaffold.
- **`monitoring/prometheus.yml` and `promtail.yml`** are vestigial — the
  kube-prometheus-stack chart does not consume them. Treat them as reference,
  not live config.
- **Helm umbrella pattern**: changes to subchart dependencies require
  `helm dependency update` before re-installing.
- **JPA `ddl-auto=update`** is set on the API. Fine for learning, will need to
  move to a migration tool (Flyway/Liquibase) before this is anything serious.

## Working preferences

- The user wants to **learn**, not just receive solutions. When making changes,
  briefly explain *why* the K8s-idiomatic approach is what it is.
- Prefer **plan-first** for any non-trivial change (new chart, new resource
  type, restructure). Lay out the steps and check in before editing.
- Don't auto-"fix" the rough edges listed above unless asked — they're known
  and the user wants to address them deliberately as learning steps.
- Don't touch `docker-compose-OLD-REFERENCE.yml` — it is intentionally frozen.
