package com.orchestrator.pipeline.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Entity
public class Pipeline {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String type;
    private LocalDateTime started;
    private LocalDateTime finished;

    @Enumerated(EnumType.STRING)
    private PipelineStatus status;

    public Pipeline() {
    }

    public Pipeline(String type, PipelineStatus status) {
        this.type = type;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDateTime getStarted() {
        return started;
    }

    public void setStarted(LocalDateTime started) {
        this.started = started;
    }

    public LocalDateTime getFinished() {
        return finished;
    }

    public void setFinished(LocalDateTime finished) {
        this.finished = finished;
    }

    public PipelineStatus getStatus() {
        return status;
    }

    public void setStatus(PipelineStatus status) {
        this.status = status;
    }
}