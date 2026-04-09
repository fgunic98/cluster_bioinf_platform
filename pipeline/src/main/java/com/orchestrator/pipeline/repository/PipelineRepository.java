package com.orchestrator.pipeline.repository;

import com.orchestrator.pipeline.model.Pipeline;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PipelineRepository extends JpaRepository<Pipeline, Long> {
}