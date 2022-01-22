/*******************************************************************************
 * Copyright (c) 2011-2014 SirSengir.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Various Contributors including, but not limited to:
 * SirSengir (original work), CovertJaguar, Player, Binnie, MysteriousAges
 ******************************************************************************/
package forestry.core.utils.datastructures;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagCollection;
import net.minecraft.tags.ItemTags;
import net.minecraft.resources.ResourceLocation;

import forestry.core.utils.ItemStackUtil;

public class ItemStackMap<T> extends StackMap<ItemStack, T> {
	private static final long serialVersionUID = -8511966739130702305L;

	@Override
	protected boolean areEqual(ItemStack a, Object b) {
		if (b instanceof ItemStack b2) {
			return ItemStackUtil.isCraftingEquivalent(b2, a);
		}
		if (b instanceof Item) {
			return a.getItem() == b;
		}
		if (b instanceof String) {
			return areEqual(a, new ResourceLocation((String) b));
		}
		if (b instanceof ResourceLocation) {
			TagCollection<Item> collection = ItemTags.getAllTags();
			Tag<Item> itemTag = collection.getTag((ResourceLocation) b);
			if (itemTag == null) {
				return false;
			}
			for (Item item : itemTag.getValues()) {
				if (areEqual(a, item)) {
					return true;
				}
			}
			return false;
		}
		return false;
	}

	@Override
	protected boolean isValidKey(Object key) {
		return key instanceof ItemStack || key instanceof Item || key instanceof String || key instanceof ResourceLocation;
	}

	@Override
	protected ItemStack getStack(Object key) {
		if (key instanceof ItemStack) {
			return (ItemStack) key;
		}
		return ItemStack.EMPTY;
	}

}
