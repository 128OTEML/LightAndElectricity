package example;

import mindustry.gen.Building;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.ui.dialogs.*;
import mindustry.world.blocks.production.*;
import mindustry.world.consumers.*;
import mindustry.world.meta.*;
import mindustry.content.*;
import mindustry.*;
import mindustry.graphics.*;
import mindustry.world.*;
import mindustry.entities.units.BuildPlan;

public class MultiRecipeFactory extends GenericCrafter {
    // 配方类定义
    public static class Recipe {
        public final String name; // 配方ID
        public final String localizedName; // 本地化名称
        public final float craftTime; // 生产时间
        public final ItemStack[] inputs; // 物品输入
        public final ItemStack[] outputs; // 物品输出
        public final LiquidStack liquidInput; // 液体输入（可选）

        // 构造函数（无液体输入）
        public Recipe(String name, String localizedName, float craftTime, ItemStack[] inputs, ItemStack[] outputs) {
            this(name, localizedName, craftTime, inputs, outputs, null);
        }

        // 构造函数（有液体输入）
        public Recipe(String name, String localizedName, float craftTime, ItemStack[] inputs, ItemStack[] outputs,
                LiquidStack liquidInput) {
            this.name = name;
            this.localizedName = localizedName;
            this.craftTime = craftTime;
            this.inputs = inputs;
            this.outputs = outputs;
            this.liquidInput = liquidInput;
        }
    }

    public Recipe[] recipes; // 工厂支持的所有配方

    public MultiRecipeFactory(String name) {
        super(name);
        // 允许手动切换配方
        configurable = true;
        saveConfig = true;
        itemCapacity = 50;
        outputsPayload = true;
    }

    @Override
    public void init() {
        super.init();
        // 初始化第一个配方的消耗
        if (recipes != null && recipes.length > 0) {
            updateRecipeConsumers(recipes[0]);
        }
    }

    // 更新工厂的消耗品（根据当前配方）
    // ... existing code ...
    // 更新工厂的消耗品（根据当前配方）
    // ... existing code ...
    // 更新工厂的消耗品（根据当前配方）
    private void updateRecipeConsumers(Recipe recipe) {
        // 重新初始化consumers数组
        consumers = new Consume[recipe.liquidInput != null ? 2 : 1];
        int index = 0;
        unloadable = false;

        // 添加物品消耗
        if (recipe.inputs != null) {
            consumers[index++] = new ConsumeItems(recipe.inputs);
        }

        // 添加液体消耗（如果有）
        if (recipe.liquidInput != null) {
            consumers[index++] = new ConsumeLiquid(recipe.liquidInput.liquid, recipe.liquidInput.amount);
        }

        // 更新生产时间
        craftTime = recipe.craftTime;

        // 设置输出物品
        outputItem = null;
        if (recipe.outputs != null && recipe.outputs.length > 0) {
            outputItem = recipe.outputs[0];
        }
    }

    // 绘制工厂顶部的配方标识
    @Override
    public void drawPlanConfig(BuildPlan req, Eachable<BuildPlan> list) {
        super.drawPlanConfig(req, list);

        if (recipes == null || recipes.length == 0)
            return;

        Recipe recipe = recipes[0]; // 默认显示第一个配方图标
        if (recipe.outputs.length > 0) {
            ItemStack output = recipe.outputs[0];
            Draw.z(Layer.blockOver);
            Draw.rect(output.item.fullIcon, req.drawx(), req.drawy() - req.block.size * 4, 16, 16);
        }
    }

    // 配方切换UI（修复配置界面方法名和参数）
// ... existing code ...
    // 配方切换UI（修复配置界面方法名和参数）
     public void buildConfiguration(Table table) {
        Building build = Vars.player.unit().buildOn();
        if (!(build instanceof MultiRecipeBuild))
            return;

        MultiRecipeBuild entity = (MultiRecipeBuild) build;

        table.add("选择配方:").row();

        Table recipesTable = new Table();
        recipesTable.defaults().size(180, 50);

        for (int i = 0; i < recipes.length; i++) {
            int index = i;
            Recipe recipe = recipes[i];
            recipesTable.button(recipe.localizedName, () -> {
                entity.currentRecipeIndex = index;
                entity.updateRecipeConsumersPublic(recipe); // 使用公共方法
                entity.configure(index); // 保存配置
            }).row();
        }

        table.add(recipesTable).row();
        table.button("关闭", () -> {
            Vars.control.input.config.hideConfig();
        }).size(100, 50);
    }
// ... existing code ...

    public class MultiRecipeBuild extends GenericCrafterBuild {
        public int currentRecipeIndex = 0; // 实例化的配方索引
        public float progress = 0f;

                // 处理配置更新（修复方法签名）
            public void updateRecipeConsumersPublic(Recipe recipe) 
            {
                updateRecipeConsumers(recipe);
            }
        // 处理配置更新（修复方法签名）
 @Override
        public void configured(Unit builder, Object value) {
            if (value instanceof Integer) {
                int index = (Integer) value;
                if (index >= 0 && index < recipes.length) {
                    currentRecipeIndex = index;
                    updateRecipeConsumers(recipes[index]);
                    progress = 0f; // 重置进度
                }
            }
        }

        // ... existing code ...
        @Override
        public void updateTile() {
            if (recipes == null || recipes.length == 0)
                return;
            Recipe recipe = recipes[currentRecipeIndex];

            // 只有在材料足够的情况下才进行生产检查和进度更新
            if (canCraft(recipe)) {
                // 检查是否生产完成
                if (progress >= 1f) {
                    craft(recipe);
                    progress = 0f;
                }

                // 更新进度
                progress += delta() / recipe.craftTime;
                if (progress >= 1f)
                    progress = 1f;
            }
            
            // 调用父类方法以确保物品能正确输出到传送带
            super.updateTile();
        }
        // 检查是否可以生产
        private boolean canCraft(Recipe recipe) {
            // 检查物品输入
            for (ItemStack stack : recipe.inputs) {
                if (items.get(stack.item) < stack.amount) {
                    return false;
                }
            }

            // 检查液体输入
            if (recipe.liquidInput != null) {
                if (liquids.get(recipe.liquidInput.liquid) < recipe.liquidInput.amount) {
                    return false;
                }
            }

            // 如果所有材料都足够，则返回true
            return true;
        }

        // 执行生产
        private void craft(Recipe recipe) {
            // 消耗物品
            for (ItemStack stack : recipe.inputs) {
                items.remove(stack.item, stack.amount);
            }

            // 消耗液体
            if (recipe.liquidInput != null) {
                liquids.remove(recipe.liquidInput.liquid, recipe.liquidInput.amount);
            }

            // 生产物品
            for (ItemStack stack : recipe.outputs) {
                for (int i = 0; i < stack.amount; i++) {
                    offload(stack.item);
                }
            }
        }

        @Override
        public void draw() {
            super.draw();
            // 绘制进度条
  //          drawProgressBar();
        }

        // 绘制进度条
        private void drawProgressBar() {
            if (recipes == null || recipes.length == 0)
                return;

            float barX = x - (size * 8f);
            float barY = y + (size * 8f) - 4f;
            float width = size * 16f;
            float height = 4f;

            // 背景
            Draw.color(Color.darkGray);
            Fill.rect(barX, barY, width, height);

            // 进度
            Draw.color(Pal.accent);
            Fill.rect(barX, barY, width * progress, height);

            // 重置颜色
            Draw.color();
        }
    }
}
