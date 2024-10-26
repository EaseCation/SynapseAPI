package org.itxtech.synapseapi.multiprotocol.common.camera;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.annotation.Nullable;

@Builder
@NoArgsConstructor
@ToString
public class CameraAimAssistCategories {
    public String identifier;
    @Default
    public Category[] categories = new Category[0];

    public CameraAimAssistCategories(String identifier, Category... categories) {
        this.identifier = identifier;
        this.categories = categories;
    }

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class Category {
        public String name;
        @Default
        public Priority[] entityPriorities = new Priority[0];
        @Default
        public Priority[] blockPriorities = new Priority[0];
        @Nullable
        public Integer defaultEntityPriority;
        @Nullable
        public Integer defaultBlockPriority;

        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        @ToString
        public static class Priority {
            public String identifier;
            public int priority;
        }
    }
}
