package example;

import arc.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.mod.*;
import mindustry.ui.dialogs.*;
import mindustry.type.*;
import mindustry.world.meta.*;

public class ExampleJavaMod extends Mod{

    public ExampleJavaMod(){
        Log.info("Loaded ExampleJavaMod constructor.");

        // 监听客户端加载事件
        Events.on(ClientLoadEvent.class, e -> {
            Time.runTask(10f, () -> {
                BaseDialog dialog = new BaseDialog("mod已加载");
                dialog.cont.add("已成功加载mod！").row();
                dialog.cont.button("确定", dialog::hide).size(100f, 50f);
                dialog.show();
            });
        });
    }

    @Override
    public void loadContent(){
        Log.info("Loading content for Multi Recipe Factory Mod.");
        
        // 创建多配方工厂实例
        MultiRecipeFactory multiFactory = new MultiRecipeFactory("multi-recipe-factory");
        
        // 设置工厂属性
        multiFactory.health = 300;
        multiFactory.size = 2;
        multiFactory.itemCapacity = 50;
        multiFactory.liquidCapacity = 100;
        multiFactory.requirements(Category.crafting, ItemStack.with(
            Items.copper, 100,
            Items.lead, 75,
            Items.silicon, 50
        ));
        multiFactory.buildVisibility = BuildVisibility.shown;
        multiFactory.description = "一个可以生产多种物品的多功能工厂。点击可切换配方。";
        
        // 定义配方1: 生产金属碎片
        MultiRecipeFactory.Recipe scrapRecipe = new MultiRecipeFactory.Recipe(
            "scrap-production",
            "金属碎片生产",
            2f, // 生产时间
            new ItemStack[]{
                new ItemStack(Items.copper, 2),
                new ItemStack(Items.lead, 1)
            },
            new ItemStack[]{new ItemStack(Items.scrap, 3)}
        );
        
        // 定义配方2: 生产硅
        MultiRecipeFactory.Recipe siliconRecipe = new MultiRecipeFactory.Recipe(
            "silicon-production",
            "硅生产",
            3f, // 生产时间
            new ItemStack[]{
                new ItemStack(Items.coal, 2),
                new ItemStack(Items.sand, 3)
            },
            new ItemStack[]{new ItemStack(Items.silicon, 1)}
        );
        
        // 定义配方3: 生产钛（使用v150兼容的物品）
        MultiRecipeFactory.Recipe titaniumRecipe = new MultiRecipeFactory.Recipe(
            "titanium-production",
            "钛生产",
            4f, // 生产时间
            new ItemStack[]{new ItemStack(Items.copper, 3)}, // 使用铜替代ore，确保兼容性
            new ItemStack[]{new ItemStack(Items.titanium, 2)},
            new LiquidStack(Liquids.water, 5f)
        );
        
        // 设置工厂的所有配方
        multiFactory.recipes = new MultiRecipeFactory.Recipe[]{
            scrapRecipe,
            siliconRecipe,
            titaniumRecipe
        };
        
        // 注册工厂到游戏
        Vars.content.blocks().add(multiFactory);
    }
}
    
