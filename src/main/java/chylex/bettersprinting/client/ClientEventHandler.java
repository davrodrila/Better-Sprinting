package chylex.bettersprinting.client;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiControls;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.GameSettings;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import chylex.bettersprinting.client.gui.GuiButtonSprint;
import chylex.bettersprinting.client.gui.GuiSprint;
import chylex.bettersprinting.client.update.UpdateNotificationManager;
import chylex.bettersprinting.system.PacketPipeline;

@SideOnly(Side.CLIENT)
public final class ClientEventHandler{
	public static void register(){
		MinecraftForge.EVENT_BUS.register(new ClientEventHandler());
	}
	
	private final Minecraft mc = Minecraft.getMinecraft();
	private boolean stopChecking;
	
	@SubscribeEvent
	public void onPlayerLoginClient(PlayerLoggedInEvent e){
		UpdateNotificationManager.run();
	}
	
	@SubscribeEvent
	public void onPlayerJoinWorld(EntityJoinWorldEvent e){
		if (stopChecking || e.getEntity() != mc.thePlayer)return;
		
		stopChecking = true;
		
		if (!mc.isIntegratedServerRunning() && mc.getCurrentServerData() != null && !ClientSettings.disableMod){
			PacketPipeline.sendToServer(ClientNetwork.writeModNotification(10));
		}
	}
	
	@SubscribeEvent
	public void onClientDisconnectedFromServer(ClientDisconnectionFromServerEvent e){
		ClientModManager.svSurvivalFlyingBoost = ClientModManager.svRunInAllDirs = ClientModManager.svDisableMod = false;
		stopChecking = false;
	}
	
	@SubscribeEvent
	public void onGuiInit(GuiScreenEvent.InitGuiEvent.Post e){
		GuiScreen gui = e.getGui();
		
		if (gui instanceof GuiControls){
			gui.buttonList.stream().filter(btn -> btn.id == GameSettings.Options.AUTO_JUMP.returnEnumOrdinal()).findFirst().ifPresent(gui.buttonList::remove);
			
			if (!(((GuiControls)gui).parentScreen instanceof GuiSprint)){
				gui.buttonList.add(0,new GuiButtonSprint(205,gui.width/2+5,18+24,150,20,"Better Sprinting"));
			}
		}
	}
	
	private ClientEventHandler(){}
}
