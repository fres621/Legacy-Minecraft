package wily.legacy.client.screen;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import wily.legacy.init.LegacySoundEvents;
import wily.legacy.util.ScreenUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class TabList implements Renderable,GuiEventListener, NarratableEntry {
    public List<TabButton> tabButtons = new ArrayList<>();
    public int selectedTab = 0;
    boolean focused = false;

    public TabList addTabButton(TabButton button){
        tabButtons.add(button);
        return this;
    }
    public TabList addTabButton(int x, int y, int width, int height, int type, ResourceLocation icon, CompoundTag itemIconTag, Component message, Tooltip tooltip, Consumer<TabButton> onPress){
        return this.addTabButton(new TabButton(x,y,width,height,type,icon,itemIconTag,message,tooltip,t-> {if (selectedTab != tabButtons.indexOf(t)) {
            selectedTab = tabButtons.indexOf(t);
            onPress.accept(t);
        }}));
    }

    public TabList addTabButton(int x, int y, int width, int height, int type, Component message, Consumer<TabButton> onPress){
        return addTabButton(x,y,width,height,type,null,null,message,null,onPress);
    }
    public TabList addTabButton(int x, int y, int height, int type, Component message, Consumer<TabButton> onPress){
        return addTabButton(x,y,0,height,type,null,null,message,null,onPress);
    }
    public TabList addTabButton(int height, int type, Component message, Consumer<TabButton> onPress){
        return addTabButton(0,0,0,height,type,null,null,message,null,onPress);
    }
    public TabList addTabButton(int height, int type, ResourceLocation icon, CompoundTag itemIconTag, Component component, Consumer<TabButton> onPress){
        return addTabButton(0,0,0,height,type,icon, itemIconTag,component, null,onPress);
    }
    public TabList addTabButton(int height, int type, ResourceLocation icon, Component component, Consumer<TabButton> onPress){
        return addTabButton(0,0,0,height,type,icon, null,component, null,onPress);
    }
    public void init(int leftPos, int topPos, int width){
        init(leftPos,topPos,width,(t,i)->{});
    }
    public void init(int leftPos, int topPos, int width,BiConsumer<TabButton, Integer> buttonManager){
        init((b,i)->{
            b.setWidth(width / tabButtons.size());
            b.setX(leftPos + i);
            b.setY(topPos);
            buttonManager.accept(b,i);
        });
    }
    public void init(BiConsumer<TabButton, Integer> buttonManager) {
        int x = 0;
        for (TabButton b : tabButtons) {
            buttonManager.accept(b,x);
            x+=b.getWidth();
        }
    }
    @Override
    public void render(GuiGraphics graphics, int i, int j, float f) {
        for (int index = 0; index < tabButtons.size(); index++) {
            TabButton tabButton = tabButtons.get(index);
            tabButton.selected = selectedTab == index;
            tabButton.render(graphics,i, j, f);
        }
    }

    @Override
    public void setFocused(boolean bl) {
        focused = bl;
    }

    @Override
    public boolean isFocused() {
        return focused;
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        return !tabButtons.stream().filter(t-> t.isHoveredOrFocused() && t.keyPressed(i,j,k)).toList().isEmpty();
    }
    public void controlTab(int i, int j, int k){
        if (i == InputConstants.KEY_LBRACKET) {
            tabButtons.get((selectedTab <= 0 ? tabButtons.size() : selectedTab) - 1).onPress();
            ScreenUtil.playSimpleUISound(LegacySoundEvents.FOCUS.get(),1.0f);
        } else if (i == InputConstants.KEY_RBRACKET) {
            tabButtons.get(selectedTab >= tabButtons.size() - 1 ? 0 : selectedTab + 1).onPress();
            ScreenUtil.playSimpleUISound(LegacySoundEvents.FOCUS.get(),1.0f);
        }
    }
    public void numberControlTab(int i){
        if (i <= 57 && i > 48 && i - 49 < tabButtons.size()) {
            tabButtons.get(i - 49).onPress();
            ScreenUtil.playSimpleUISound(LegacySoundEvents.FOCUS.get(),1.0f);
        }
    }
    @Override
    public boolean mouseClicked(double d, double e, int i) {
        return !tabButtons.stream().filter(t-> t.mouseClicked(d,e,i)).toList().isEmpty();
    }
    public boolean isMouseOver(double d, double e) {
        return !tabButtons.stream().filter(t-> t.isMouseOver(d,e)).toList().isEmpty();
    }

    @Override
    public NarrationPriority narrationPriority() {
        return this.tabButtons.stream().map(AbstractWidget::narrationPriority).max(Comparator.naturalOrder()).orElse(NarrationPriority.NONE);
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {
        Optional<TabButton> optional = this.tabButtons.stream().filter(AbstractWidget::isHovered).findFirst().or(() -> Optional.ofNullable(tabButtons.get(selectedTab)));
        optional.ifPresent(tabButton -> {
            narrationElementOutput.add(NarratedElementType.POSITION, Component.translatable("narrator.position.tab", selectedTab + 1, tabButtons.size()));
            tabButton.updateNarration(narrationElementOutput);
        });
        if (this.isFocused()) {
            narrationElementOutput.add(NarratedElementType.USAGE,  Component.translatable("narration.tab_navigation.usage"));
        }
    }
}
