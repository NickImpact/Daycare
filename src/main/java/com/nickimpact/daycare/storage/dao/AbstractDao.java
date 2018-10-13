/*
 * This file is part of LuckPerms, licensed under the MIT License.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package com.nickimpact.daycare.storage.dao;

import com.nickimpact.daycare.DaycarePlugin;
import com.nickimpact.daycare.ranch.DaycareNPC;
import com.nickimpact.daycare.ranch.Ranch;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractDao {

	@Getter
	protected final DaycarePlugin plugin;

	@Getter
	public final String name;

	public abstract void init() throws Exception;

	public abstract void shutdown() throws Exception;

	public abstract void addRanch(Ranch ranch) throws Exception;

	public abstract void updateRanch(Ranch ranch) throws Exception;

	public abstract void updateAll(List<Ranch> ranches) throws Exception;

	public abstract void deleteRanch(UUID uuid) throws Exception;

	public abstract Ranch getRanch(UUID uuid) throws Exception;

	public abstract List<Ranch> getAllRanches() throws Exception;

	public abstract void addNPC(DaycareNPC npc) throws Exception;

	public abstract void deleteNPC(DaycareNPC npc) throws Exception;

	public abstract List<DaycareNPC> getNPCS() throws Exception;

	public abstract void purge(boolean logs) throws Exception;

	public abstract void save() throws Exception;
}
