package haoke.speeck.receive;

import com.haokegroup.android.hkserver.manager.HkManager;

import haoke.data.mcuhelp.McuHelp;
import haoke.lyb.system.HKWindowManager;
import haoke.ui.bt.BT_Activity_Main;
import haoke.ui.bt.BT_IF;
import haoke.ui.bt.BT_Tab;
import haoke.ui.pub.Pub_Air_Quality_Tip;
import jsbd.ui.util.ConfigureUtil;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import canbus.zotye.data.Zotye;

public class SpeechReceiver extends BroadcastReceiver {

	private static final String TAG = "SpeechReceiver";
	
	private static final String ACTION_JDBDTEK_LAUNCHER = "com.jsbdtek.os.action";
	

	private static final String ACTION_JDBDTEK_PHONE = "com.jsbdtek.phone.action";
	

	private static final String KEY_CALL_NUMBER = "callNumber";// 联系人号码
	private static final String KEY_COMMAND_CODE = "commandCode";//

	private static final int SR_PHONE_CONTACTS = 60;// 进入电话本界面
	private static final int SR_PHONE_HISTORYCALL = 61;// 进入到拨号记录界面
	private static final int SR_PHONE_DIAL = 62;// 进入到拨号面板界面
	private static final int SR_PHONE_CALL = 63;// 平台提供可以获取联系人数据的接口，语音助理识别后将电话号码发给平台，然后平台负责拨号

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		String action = intent.getAction();
		Log.i(TAG, "ZJF--onReceive---intent" + action);
		// 蓝牙电话相关广播
		if (intent.getAction().equals(ACTION_JDBDTEK_PHONE)) {
			int cmdCode = intent.getIntExtra(KEY_COMMAND_CODE, 0);
			Log.i(TAG, "ZJF--onReceive--cmdCode=" + cmdCode);
			boolean bResult = false;// 返回结果状态
			int iResultCode = 220;// 返回结果的提示编码
			int iToFragment=0;
			int iBTStatus=HkManager.getInstance().getBt().getConnectState();
			Log.i(TAG, "ZJF--onReceive--iBTStatus="+iBTStatus);
			Log.i(TAG, "ZJF--onReceive--BT_IF.getInstance().IsConnected()="+BT_IF.getInstance().IsConnected());
			if ((BT_IF.getInstance().IsConnected()==false)&& (cmdCode>59&&cmdCode<64)) {// 蓝牙未连接
				iResultCode=220;
				bResult = false;
				Log.i(TAG, "zjf--onReceive notcon");
//				HKWindowManager.getInstance().OpenActivityByPkgName(HKWindowManager.PKG_NAME_BT);

			} else {
				switch (cmdCode) {
				case SR_PHONE_CONTACTS:// 进入电话本界面
					iToFragment=1;
					bResult = true;
					iResultCode = 210;
					openBtActivity(context, 1);
					break;
				case SR_PHONE_HISTORYCALL:// 进入到拨号记录界面
					iToFragment=2;
					bResult = true;
					iResultCode = 211;
					openBtActivity(context, 2);
					break;
				case SR_PHONE_DIAL:// 进入到拨号面板界面
					iToFragment=3;
					bResult = true;
					iResultCode = 212;
					openBtActivity(context, 3);
					break;
				case SR_PHONE_CALL:// 获取电话号码并拨号
					String DialNum = intent.getStringExtra(KEY_CALL_NUMBER);
					Log.i(TAG, "ZJF--onReceive--DialNum="+DialNum);
					McuHelp.mBTIF.dialNumber(DialNum);
					bResult = true;
					iResultCode = 213;
					break;
				default:
					break;
				}
			}
			UISendBroadcast(context, bResult, iResultCode);// 向语音发送广播返回结果
		}else if (intent.getAction().equals(ACTION_JDBDTEK_LAUNCHER)) {
			int cmdCode = intent.getIntExtra(KEY_COMMAND_CODE, -1);
			switch(cmdCode){
			case 81://到蓝牙连接界面
				HKWindowManager.getInstance().OpenActivityByPkgName(HKWindowManager.PKG_NAME_SETTING);
				break;
			case 72://到车辆信息界面
				HKWindowManager.getInstance().OpenActivityByPkgName(HKWindowManager.PKG_NAME_CARINFO);
				break;
			case 73://到电话界面
				openBtActivity(context, 3);
				break;
			case 75:
				ConfigureUtil.setCarInfoValue(100);
				HKWindowManager.getInstance().OpenActivityByPkgName(HKWindowManager.PKG_NAME_CANBUS);
				break;
			case 76:
				ConfigureUtil.setCarInfoValue(101);
				HKWindowManager.getInstance().OpenActivityByPkgName(HKWindowManager.PKG_NAME_CANBUS);
				break;
			case 77:
				ConfigureUtil.setCarInfoValue(102);
				HKWindowManager.getInstance().OpenActivityByPkgName(HKWindowManager.PKG_NAME_CANBUS);
				break;
			case 79:
				Pub_Air_Quality_Tip.getInstance().ShowTip(Zotye.als_AirInfo.mInsidePM, Zotye.als_AirInfo.mOutsidePM);
				break;
			case 80:
				Pub_Air_Quality_Tip.getInstance().CloseTip();
				break;
			}
		}
		

	}

	private void openBtActivity(Context context,int key){
		Log.i(TAG, "openBtActivity  key="+key);
		BT_Tab.setCurType(key);
		Intent intent = new Intent(context, BT_Activity_Main.class);
		intent.putExtra("keyToFragment", key);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}
	
	
	public void UISendBroadcast(Context context, boolean bResult, int iCode) {
		int restCode = 0;
		if (bResult == true)// 成功
		{
			switch (iCode) {
			case 210:
				break;
			}
			restCode = 1;
		} else // 失败
		{
			restCode = 0;
		}
		Intent intent = new Intent();
		intent.setAction("com.jsbdtek.result.action");//
		intent.putExtra("resultCode", restCode);
		intent.putExtra("resultDescribe", iCode);
		context.sendBroadcast(intent);
	}

}
