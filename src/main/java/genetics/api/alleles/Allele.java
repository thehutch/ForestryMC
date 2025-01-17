package genetics.api.alleles;

import com.google.common.base.MoreObjects;

import java.util.Objects;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import net.minecraftforge.registries.ForgeRegistryEntry;

/**
 * A default implementation of a simple allele.
 */
public class Allele extends ForgeRegistryEntry<IAllele> implements IAllele {
	public static final IAllele EMPTY = new Allele("empty", false).setRegistryName(new ResourceLocation("genetics", "empty"));

	protected final boolean dominant;
	protected final String localisationKey;

	public Allele(String localisationKey, boolean dominant) {
		this.localisationKey = localisationKey;
		this.dominant = dominant;
	}

	@Override
	public boolean isDominant() {
		return dominant;
	}

	@Override
	public int hashCode() {
		return getRegistryName() != null ? getRegistryName().hashCode() : Objects.hash(dominant);
	}

	@Override
	public Component getDisplayName() {
		return new TranslatableComponent(getLocalisationKey());
	}

	@Override
	public String getLocalisationKey() {
		return localisationKey;
	}

	@Override
	public IAlleleType getType() {
		return null;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof IAllele otherAllele)) {
			return false;
		}
		return getRegistryName() != null ?
			getRegistryName().equals(((IAllele) obj).getRegistryName()) :
			dominant == otherAllele.isDominant();
	}

	@Override
	public String toString() {
		return MoreObjects
			.toStringHelper(this)
			.add("name", getRegistryName())
			.add("dominant", dominant)
			.add("key", localisationKey)
			.toString();
	}
}
