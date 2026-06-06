package com.elderwatch.client.models;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OverviewCard {
    private String label;
    private String count;
    private int imageResource;
    private int position;

    public OverviewCard(Builder builder) {
        this.label = builder.label;
        this.count = builder.count;
        this.imageResource = builder.imageResource;
        this.position = builder.position;
    }

    public static class Builder {
        private String label;
        private String count;
        private int imageResource;
        private int position;

        public Builder setPosition(int position) {
            this.position = position;
            return this;
        }

        public Builder setImageResource(int imageResource) {
            this.imageResource = imageResource;
            return this;
        }

        public Builder setLabel(String label) {
            this.label = label;
            return this;
        }

        public Builder setCount(String count) {
            this.count = count;
            return this;
        }

        public OverviewCard build() {
            return new OverviewCard(this);
        }
    }
}
