package com.omnitools.omniTools.mixin.ae2;

import appeng.parts.p2p.P2PTunnelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(P2PTunnelPart.class)
public interface P2PTunnelPartInvoker {
    @Invoker("setOutput")
    void omnitools$invokeSetOutput(boolean output);
}
