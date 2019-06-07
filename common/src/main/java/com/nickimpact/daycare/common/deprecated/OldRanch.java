package com.nickimpact.daycare.common.deprecated;

import com.nickimpact.daycare.api.pens.Pen;
import com.nickimpact.daycare.api.pens.Settings;
import com.nickimpact.daycare.api.pens.Statistics;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@Deprecated
public class OldRanch {

	private UUID ownerUUID;
	private List<OldPen> pens;
	private Statistics stats;
	private Settings settings;

}
