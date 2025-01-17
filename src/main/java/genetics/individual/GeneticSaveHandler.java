package genetics.individual;

import javax.annotation.Nullable;

import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

import genetics.ApiInstance;
import genetics.Log;
import genetics.api.GeneticHelper;
import genetics.api.IGeneticSaveHandler;
import genetics.api.alleles.IAllele;
import genetics.api.alleles.IAlleleRegistry;
import genetics.api.alleles.IAlleleTemplate;
import genetics.api.individual.IChromosome;
import genetics.api.individual.IChromosomeType;
import genetics.api.individual.IGenome;
import genetics.api.individual.IIndividual;
import genetics.api.individual.IKaryotype;
import genetics.api.organism.IOrganismHandler;
import genetics.api.organism.IOrganismType;
import genetics.api.root.IIndividualRoot;
import genetics.api.root.ITemplateContainer;

public enum GeneticSaveHandler implements IGeneticSaveHandler {
	INSTANCE;
	private static final String GENOME_TAG = "Genome";
	private static SaveFormat writeFormat = SaveFormat.UID;

	public static void setWriteFormat(SaveFormat writeFormat) {
		GeneticSaveHandler.writeFormat = writeFormat;
	}

	@Override
	public CompoundTag writeTag(IChromosome[] chromosomes, IKaryotype karyotype, CompoundTag tagCompound) {
		return writeFormat.writeTag(chromosomes, karyotype, tagCompound);
	}

	@Override
	public IChromosome[] readTag(IKaryotype karyotype, CompoundTag tagCompound) {
		SaveFormat format = getFormat(tagCompound);
		return format.readTag(karyotype, tagCompound);
	}

	private SaveFormat getFormat(CompoundTag tagCompound) {
		for (SaveFormat format : SaveFormat.values()) {
			if (format.canLoad(tagCompound)) {
				return format;
			}
		}
		return SaveFormat.UID;
	}

	@Override
	@Nullable
	public IAllele getAlleleDirectly(CompoundTag genomeNBT, IChromosomeType chromosomeType, boolean active) {
		SaveFormat format = getFormat(genomeNBT);
		return format.getAlleleDirectly(genomeNBT, chromosomeType, active);
	}

	/**
	 * Quickly gets the species without loading the whole genome. And without creating absent chromosomes.
	 */
	@Override
	@Nullable
	public IAllele getAlleleDirectly(ItemStack itemStack, IOrganismType type, IChromosomeType chromosomeType, boolean active) {
		CompoundTag nbtTagCompound = itemStack.getTag();
		if (nbtTagCompound == null || nbtTagCompound.isEmpty()) {
			return null;
		}

		CompoundTag individualNBT = getIndividualDataDirectly(itemStack, type, chromosomeType.getRoot());
		if (individualNBT == null || individualNBT.isEmpty()) {
			return null;
		}

		CompoundTag genomeNBT = individualNBT.getCompound(GENOME_TAG);
		if (genomeNBT.isEmpty()) {
			return null;
		}
		IAllele allele = getAlleleDirectly(genomeNBT, chromosomeType, active);
		IAlleleRegistry alleleRegistry = ApiInstance.INSTANCE.getAlleleRegistry();
		if (allele == null || !alleleRegistry.isValidAllele(allele, chromosomeType)) {
			return null;
		}
		return allele;
	}

	// NBT RETRIEVAL

	@Override
	public IAllele getAllele(ItemStack itemStack, IOrganismType type, IChromosomeType chromosomeType, boolean active) {
		IChromosome chromosome = getSpecificChromosome(itemStack, type, chromosomeType);
		return active ? chromosome.getActiveAllele() : chromosome.getInactiveAllele();
	}

	@Override
	public IChromosome getSpecificChromosome(CompoundTag genomeNBT, IChromosomeType chromosomeType) {
		SaveFormat format = getFormat(genomeNBT);
		return format.getSpecificChromosome(genomeNBT, chromosomeType);
	}

	/**
	 * Tries to load a specific chromosome and creates it if it is absent.
	 */
	@Override
	public IChromosome getSpecificChromosome(ItemStack itemStack, IOrganismType type, IChromosomeType chromosomeType) {
		CompoundTag nbtTagCompound = itemStack.getTag();
		if (nbtTagCompound == null) {
			nbtTagCompound = new CompoundTag();
			itemStack.setTag(nbtTagCompound);
		}

		CompoundTag individualNBT = getIndividualData(itemStack, type, chromosomeType.getRoot());
		CompoundTag genomeNBT = individualNBT.getCompound(GENOME_TAG);

		return getSpecificChromosome(genomeNBT, chromosomeType);
	}

	@Nullable
	@Override
	public CompoundTag getIndividualDataDirectly(ItemStack itemStack, IOrganismType type, IIndividualRoot<IIndividual> root) {
		IOrganismHandler organismHandler = GeneticHelper.getOrganismHandler(root, type);
		return organismHandler.getIndividualData(itemStack);
	}

	@Override
	public void setIndividualData(ItemStack itemStack, IOrganismType type, IIndividualRoot<IIndividual> root, CompoundTag compound) {
		IOrganismHandler organismHandler = GeneticHelper.getOrganismHandler(root, type);
		organismHandler.setIndividualData(itemStack, compound);
	}

	@Override
	public CompoundTag getIndividualData(ItemStack itemStack, IOrganismType type, IIndividualRoot<IIndividual> root) {
		IOrganismHandler organismHandler = GeneticHelper.getOrganismHandler(root, type);
		CompoundTag compound = organismHandler.getIndividualData(itemStack);
		if (compound != null) {
			return compound;
		}
		compound = new CompoundTag();
		CompoundTag genomeNBT = compound.getCompound(GENOME_TAG);

		if (genomeNBT.isEmpty()) {
			Log.error("Got a genetic item with no genome, setting it to a default value.");
			genomeNBT = new CompoundTag();

			ITemplateContainer container = root.getTemplates();
			IAlleleTemplate defaultTemplate = container.getKaryotype().getDefaultTemplate();
			IGenome genome = defaultTemplate.toGenome(null);
			genome.writeToNBT(genomeNBT);
			compound.put(GENOME_TAG, genomeNBT);
		}
		organismHandler.setIndividualData(itemStack, compound);
		return compound;
	}
}
