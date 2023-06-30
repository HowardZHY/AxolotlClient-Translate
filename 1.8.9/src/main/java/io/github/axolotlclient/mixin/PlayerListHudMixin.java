/*
 * Copyright © 2021-2023 moehreag <moehreag@gmail.com> & Contributors
 *
 * This file is part of AxolotlClient.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * For more information, see the LICENSE file.
 */

package io.github.axolotlclient.mixin;

import java.util.List;
import java.util.UUID;

import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.AxolotlClientConfig.Color;
import io.github.axolotlclient.modules.hypixel.HypixelAbstractionLayer;
import io.github.axolotlclient.modules.hypixel.bedwars.BedwarsGame;
import io.github.axolotlclient.modules.hypixel.bedwars.BedwarsMod;
import io.github.axolotlclient.modules.hypixel.bedwars.BedwarsPlayer;
import io.github.axolotlclient.modules.hypixel.levelhead.LevelHeadMode;
import io.github.axolotlclient.modules.hypixel.nickhider.NickHider;
import io.github.axolotlclient.modules.tablist.Tablist;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.network.ClientConnection;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(PlayerListHud.class)
public abstract class PlayerListHudMixin extends DrawableHelper {

	@Shadow @Final
	private final MinecraftClient client = MinecraftClient.getInstance();
	@Shadow
	private Text header;
	@Shadow
	private Text footer;
	private PlayerListEntry playerListEntry;

	@Inject(method = "getPlayerName", at = @At("HEAD"), cancellable = true)
	public void axolotlclient$nickHider(PlayerListEntry playerEntry, CallbackInfoReturnable<String> cir) {
		if (playerEntry.getProfile().getId() == MinecraftClient.getInstance().player.getUuid()
			&& NickHider.getInstance().hideOwnName.get()) {
			cir.setReturnValue(NickHider.getInstance().hiddenNameSelf.get());
		} else if (playerEntry.getProfile().getId() != MinecraftClient.getInstance().player.getUuid()
			&& NickHider.getInstance().hideOtherNames.get()) {
			cir.setReturnValue(NickHider.getInstance().hiddenNameOthers.get());
		}
	}

	@ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/PlayerListHud;getPlayerName(Lnet/minecraft/client/network/PlayerListEntry;)Ljava/lang/String;"))
	public PlayerListEntry axolotlclient$getPlayer(PlayerListEntry playerEntry) {
		playerListEntry = playerEntry;
		return playerEntry;
	}

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;getStringWidth(Ljava/lang/String;)I", ordinal = 0))
	public int axolotlclient$moveName(TextRenderer instance, String text) {
		if (AxolotlClient.CONFIG.showBadges.get() && AxolotlClient.isUsingClient(playerListEntry.getProfile().getId()))
			return instance.getStringWidth(text) + 10;
		return instance.getStringWidth(text);
	}

	@ModifyArgs(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;drawWithShadow(Ljava/lang/String;FFI)I", ordinal = 1))
	public void axolotlclient$getCoords(Args args) {
		float x = args.get(1);
		float y = args.get(2);
		if (AxolotlClient.CONFIG.showBadges.get()
			&& AxolotlClient.isUsingClient(playerListEntry.getProfile().getId())) {
			client.getTextureManager().bindTexture(AxolotlClient.badgeIcon);
			DrawableHelper.drawTexture((int) x, (int) y, 0, 0, 8, 8, 8, 8);
			args.set(1, x + 10);
		}
	}

	@ModifyArgs(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;drawWithShadow(Ljava/lang/String;FFI)I", ordinal = 2))
	public void axolotlclient$getCoords2(Args args) {
		float x = args.get(1);
		float y = args.get(2);
		if (AxolotlClient.CONFIG.showBadges.get()
			&& AxolotlClient.isUsingClient(playerListEntry.getProfile().getId())) {
			client.getTextureManager().bindTexture(AxolotlClient.badgeIcon);
			DrawableHelper.drawTexture((int) x, (int) y, 0, 0, 8, 8, 8, 8);
			args.set(1, x + 10);
		}
	}

	@Inject(method = "renderLatencyIcon", at = @At("HEAD"), cancellable = true)
	private void axolotlclient$numericalPing(int width, int x, int y, PlayerListEntry entry, CallbackInfo ci) {
		if (Tablist.getInstance().renderNumericPing(width, x, y, entry)) {
			ci.cancel();
		}
	}

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;isIntegratedServerRunning()Z"))
	private boolean axolotlclient$showPlayerHeads$1(MinecraftClient instance) {
		if (Tablist.getInstance().showPlayerHeads.get()) {
			return instance.isIntegratedServerRunning();
		}
		return false;
	}

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/ClientConnection;isEncrypted()Z"))
	private boolean axolotlclient$showPlayerHeads$2(ClientConnection instance) {
		if (Tablist.getInstance().showPlayerHeads.get()) {
			return instance.isEncrypted();
		}
		return false;
	}

	@Inject(method = "render", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/hud/PlayerListHud;header:Lnet/minecraft/text/Text;"))
	private void axolotlclient$setRenderHeaderFooter(int width, Scoreboard scoreboard, ScoreboardObjective playerListScoreboardObjective, CallbackInfo ci) {
		if (!Tablist.getInstance().showHeader.get()) {
			header = null;
		}
		if (!Tablist.getInstance().showFooter.get()) {
			footer = null;
		}
	}

	@ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;getPlayerByUuid(Ljava/util/UUID;)Lnet/minecraft/entity/player/PlayerEntity;"))
	private UUID axolotlclient$makeStuff(UUID par1) {
		return Tablist.getInstance().alwaysShowHeadLayer.get() ? MinecraftClient.getInstance().player.getUuid() : par1;
	}

	@Inject(
		method = "render",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/hud/PlayerListHud;renderLatencyIcon(IIILnet/minecraft/client/network/PlayerListEntry;)V"
		),
		locals = LocalCapture.CAPTURE_FAILHARD
	)
	public void axolotlclient$renderWithoutObjective(
		int width, Scoreboard scoreboard, ScoreboardObjective playerListScoreboardObjective, CallbackInfo ci,
		ClientPlayNetworkHandler clientPlayNetworkHandler, List list, int i, int j, int l, int m, int k, boolean bl, int n, int o,
		int p, int q, int r, List list2, int t, int u, int s, int v, int y, PlayerListEntry playerListEntry2
	) {
		if (!BedwarsMod.getInstance().isEnabled() || !BedwarsMod.getInstance().isWaiting()) {
			return;
		}
		int startX = v + i + 1;
		int endX = startX + n;
		String render;
		try {
			if(playerListEntry2.getProfile().getName().contains(Formatting.OBFUSCATED.toString())){
				return;
			}

			render = String.valueOf(HypixelAbstractionLayer.getPlayerLevel(playerListEntry2
					.getProfile().getId().toString().replace("-", ""),
				LevelHeadMode.BEDWARS.toString()));
		} catch (Exception e) {
			return;
		}
		this.client.textRenderer.drawWithShadow(
			render,
			(float)(endX - this.client.textRenderer.getStringWidth(render)) + 20,
			(float) y,
			-1
		);
	}

	@Inject(
		method = "renderLatencyIcon",
		at = @At("HEAD"),
		cancellable = true
	)
	public void axolotlclient$cancelLatencyIcon(int width, int x, int y, PlayerListEntry playerEntry, CallbackInfo ci) {
		if (BedwarsMod.getInstance().isEnabled() && BedwarsMod.getInstance().blockLatencyIcon() && (BedwarsMod.getInstance().isWaiting() || BedwarsMod.getInstance().inGame())) {
			ci.cancel();
		}
	}

	@Inject(
		method = "renderScoreboardObjective",
		at = @At(
			value="INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;drawWithShadow(Ljava/lang/String;FFI)I", ordinal=1
		),
		cancellable = true
	)
	public void axolotlclient$renderCustomScoreboardObjective(
		ScoreboardObjective objective, int y, String player, int startX, int endX, PlayerListEntry playerEntry, CallbackInfo ci
	) {
		if (!BedwarsMod.getInstance().isEnabled()) {
			return;
		}

		BedwarsGame game = BedwarsMod.getInstance().getGame().orElse(null);
		if (game == null) {
			return;
		}
		BedwarsPlayer bedwarsPlayer = game.getPlayer(playerEntry.getProfile().getName()).orElse(null);
		if (bedwarsPlayer == null) {
			return;
		}
		ci.cancel();
		String render;
		int color;
		if (!bedwarsPlayer.isAlive()) {
			if (bedwarsPlayer.isDisconnected()) {
				return;
			}
			int tickTillLive = Math.max(0, bedwarsPlayer.getTickAlive() - this.client.inGameHud.getTicks());
			float secondsTillLive = tickTillLive / 20f;
			render = String.format("%.1f", secondsTillLive) + "s";
			color = new Color(200, 200, 200).getAsInt();
		} else {
			int health = objective.getScoreboard().getPlayerScore(player, objective).getScore();
			color = Color.blend(new Color(255,255,255), new Color(215, 0, 64), (int) (1 - (health / 20f)*100)).getAsInt();
			render = String.valueOf(health);
		}
		// Health
		this.client.textRenderer.drawWithShadow(
			render,
			(float)(endX - this.client.textRenderer.getStringWidth(render)),
			(float) y,
			color
		);

	}

	@ModifyVariable(
		method = "render",
		at = @At(
			value="STORE"
		),
		ordinal = 7
	)
	public int axolotlclient$changeWidth(int value) {
		if (BedwarsMod.getInstance().isEnabled() && BedwarsMod.getInstance().blockLatencyIcon() && (BedwarsMod.getInstance().isWaiting() || BedwarsMod.getInstance().inGame())) {
			value -= 9;
		}
		if (BedwarsMod.getInstance().isEnabled() && BedwarsMod.getInstance().isWaiting()) {
			value += 20;
		}
		return value;
	}

	@Inject(method = "getPlayerName", at = @At("HEAD"), cancellable = true)
	public void axolotlclient$getPlayerName(PlayerListEntry playerEntry, CallbackInfoReturnable<String> cir) {
		if (!BedwarsMod.getInstance().isEnabled()) {
			return;
		}
		BedwarsGame game = BedwarsMod.getInstance().getGame().orElse(null);
		if (game == null || !game.isStarted()) {
			return;
		}
		BedwarsPlayer player = game.getPlayer(playerEntry.getProfile().getName()).orElse(null);
		if (player == null) {
			return;
		}
		cir.setReturnValue(player.getTabListDisplay());
	}

	@ModifyVariable(method = "render", at = @At(value = "INVOKE_ASSIGN", target = "Lcom/google/common/collect/Ordering;sortedCopy(Ljava/lang/Iterable;)Ljava/util/List;", remap = false))
	public List<PlayerListEntry> axolotlclient$overrideSortedPlayers(List<PlayerListEntry> original) {
		if (!BedwarsMod.getInstance().inGame()) {
			return original;
		}
		List<PlayerListEntry> players = BedwarsMod.getInstance().getGame().get().getTabPlayerList(original);
		if (players == null) {
			return original;
		}
		return players;
	}

	@Inject(method = "setHeader", at = @At("HEAD"), cancellable = true)
	public void axolotlclient$changeHeader(Text header, CallbackInfo ci) {
		if (!BedwarsMod.getInstance().inGame()) {
			return;
		}
		this.header = BedwarsMod.getInstance().getGame().get().getTopBarText();
		ci.cancel();
	}

	@Inject(method = "setFooter", at = @At("HEAD"), cancellable = true)
	public void axolotlclient$changeFooter(Text header, CallbackInfo ci) {
		if (!BedwarsMod.getInstance().inGame()) {
			return;
		}
		this.footer = BedwarsMod.getInstance().getGame().get().getBottomBarText();
		ci.cancel();
	}
}
