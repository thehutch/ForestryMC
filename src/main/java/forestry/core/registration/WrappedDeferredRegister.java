package forestry.core.registration;

import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryObject;

public class WrappedDeferredRegister<T extends IForgeRegistryEntry<T>> {

	protected final DeferredRegister<T> internal;

	protected WrappedDeferredRegister(String modid, IForgeRegistry<T> registry) {
		internal = DeferredRegister.create(registry, modid);
	}

	/**
	 * @apiNote For use with custom registries
	 */
	protected WrappedDeferredRegister(String modid, Class<T> base) {
		internal = DeferredRegister.create(base, modid);
	}

	protected <I extends T, W extends WrappedRegistryObject<I>> W register(String name, Supplier<? extends I> sup, Function<RegistryObject<I>, W> objectWrapper) {
		return objectWrapper.apply(internal.register(name, sup));
	}

	public void register(IEventBus bus) {
		internal.register(bus);
	}
}
