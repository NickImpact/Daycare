package com.nickimpact.daycare.api.util;

import com.nickimpact.daycare.api.IDaycarePlugin;
import lombok.Getter;
import lombok.Setter;

public class PluginInstance {

	@Getter @Setter
	private static IDaycarePlugin plugin;

}
