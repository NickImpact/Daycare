package com.nickimpact.daycare.api.breeding;

import lombok.Getter;

@Getter
public enum BreedStage {

    SETTLING(29, 30, 31, 32),
    SOCIALIZING(30, 31, 32),
    IN_LOVE(31, 32),
    OUT_ON_THE_TOWN(32),
    ONE_NIGHT_STAND,
    BRED;

    private int[] slots;

    BreedStage(int... slots) {
        this.slots = slots;
    }
}
