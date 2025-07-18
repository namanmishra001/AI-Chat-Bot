package com.ai.bot.model;

import jakarta.persistence.*;

@Entity
@Table(name = "Case_Vector")
public class CaseVector {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // "Technical" or "Non-Technical
    private String label;

    // Vector representation of the description
    private float[] vector;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public float[] getVector() {
        return vector;
    }

    public void setVector(float[] vector) {
        this.vector = vector;
    }
}
