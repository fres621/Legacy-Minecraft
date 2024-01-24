package wily.legacy.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import wily.legacy.LegacyMinecraft;
import wily.legacy.LegacyMinecraftClient;
import wily.legacy.client.LegacyWorldTemplate;

import java.io.IOException;
import java.util.function.Consumer;

public class CreationList extends RenderableVList{
    private PlayGameScreen screen;
    protected final Minecraft minecraft;

    public CreationList() {
        layoutSpacing(l->0);
        minecraft = Minecraft.getInstance();
        addCreationButton(this,new ResourceLocation(LegacyMinecraft.MOD_ID,"creation_list/create_world"),Component.translatable("legacy.menu.create_world"),c-> CreateWorldScreen.openFresh(this.minecraft, screen));
        LegacyWorldTemplate.list.forEach(t-> addCreationButton(this,t.icon(),t.buttonName(), c-> {
            try {
                String name = LegacyMinecraftClient.importSaveFile(minecraft,minecraft.getResourceManager().getResourceOrThrow(t.worldTemplate()).open(),t.folderName());
                if (t.directJoin()) minecraft.createWorldOpenFlows().loadLevel(screen,name);
                else {
                    LevelStorageSource.LevelStorageAccess access = minecraft.getLevelSource().createAccess(name);
                    LevelSummary summary = access.getSummary();
                    if (summary != null)
                        minecraft.setScreen(new LoadSaveScreen(screen,summary, access,true));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }));
    }

    @Override
    public void init(Screen screen, int leftPos, int topPos, int listWidth, int listHeight) {
        if (screen instanceof PlayGameScreen s) this.screen = s;
        super.init(screen, leftPos, topPos, listWidth, listHeight);
    }

    public static void addCreationButton(RenderableVList list, ResourceLocation iconSprite, Component message, Consumer<AbstractButton> onPress){
        list.addRenderable(new AbstractButton(0,0,270,30,message) {
            @Override
            protected void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
                super.renderWidget(guiGraphics, i, j, f);
                RenderSystem.enableBlend();
                guiGraphics.blitSprite(iconSprite, getX() + 5, getY() + 5, 20, 20);
                RenderSystem.disableBlend();
                if (Minecraft.getInstance().options.touchscreen().get().booleanValue() || isHovered) {
                    guiGraphics.fill(getX() + 5, getY() + 5, getX() + 25, getY() + 25, -1601138544);
                }
            }
            @Override
            protected void renderScrollingString(GuiGraphics guiGraphics, Font font, int i, int j) {
                int k = this.getX() + 35;
                int l = this.getX() + this.getWidth();
                TickBox.renderScrollingString(guiGraphics, font, this.getMessage(), k, this.getY(), l, this.getY() + this.getHeight(), j, true);
            }
            @Override
            public void onPress() {
                onPress.accept(this);
            }

            @Override
            protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
                defaultButtonNarrationText(narrationElementOutput);
            }
        });
    }

    public PlayGameScreen getScreen() {
        return this.screen;
    }


}
