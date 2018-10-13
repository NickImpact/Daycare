package com.nickimpact.daycare.api.breeding;

import com.nickimpact.impactor.api.registers.DataRegister;
import com.nickimpact.impactor.json.Registry;
import lombok.Getter;

import java.util.Optional;

public class BreedStyleRegistry extends DataRegister<BreedStyle> {

	@Getter private Registry<BreedStyle.Instance> instanceRegistry = new Registry<>();

	public BreedStyle getFirst() {
		return this.data.get(0);
	}

	public Optional<BreedStyle> getFromName(String name) {
		for(BreedStyle style : this.data) {
			if(style.getID().equalsIgnoreCase(name)) {
				return Optional.of(style);
			}
		}

		return Optional.empty();
	}
}
