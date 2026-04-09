package com.orchestrator.pipeline.controller;

import com.orchestrator.pipeline.model.Pipeline;
import com.orchestrator.pipeline.repository.PipelineRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pipelines")
public class PipelineController {

    private final PipelineRepository pipelineRepository;

    public PipelineController(PipelineRepository pipelineRepository) {
        this.pipelineRepository = pipelineRepository;
    }

    @GetMapping
    public List<Pipeline> getAllPipelines() {
        return pipelineRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pipeline> getPipelineById(@PathVariable Long id) {
        return pipelineRepository.findById(id)
                .map(pipeline -> ResponseEntity.ok(pipeline))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public Pipeline createPipeline(@RequestBody Pipeline pipeline) {
        return pipelineRepository.save(pipeline);
    }
}