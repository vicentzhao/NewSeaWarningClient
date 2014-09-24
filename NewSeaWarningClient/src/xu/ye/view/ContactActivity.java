package xu.ye.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;

import xu.ye.R;
import xu.ye.bean.ContactBean;
import xu.ye.uitl.BaseIntentUtil;
import xu.ye.uitl.StringUtil;
import xu.ye.view.adapter.ContactAdapter;
import xu.ye.view.sms.MessageBoxList;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ContactActivity extends Activity implements OnItemClickListener{

	public static final int UPDATE_VIEW_TIPS = 1;
	public static final int HIDDEN_VIEW_TIPS = 2;

	private int sideBarWidth = 80;
	private long timeToDelay = 1500;
	private TextView scrollTipView;
	private SideBar indexBar;
	private Button addContactBtn; //添加联系人
	private java.util.Timer timer;
	private TimerTask task;
   
//	private String[] lianxiren1 = new String[] { "拨打电话", "发送短信", "查看详细","移动分组","移出群组","删除" };
	private String[] lianxiren1 = new String[] { "拨打电话", "发送短信", "查看详细","删除" };
	private ContactAdapter adapter;
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case UPDATE_VIEW_TIPS:
				if (scrollTipView != null) {
					
					/*LayoutParams layoutParams = indexBar.getLayoutParams();
					layoutParams.width = sideBarWidth * 2;
					indexBar.setLayoutParams(layoutParams);*/
					String tips = msg.obj.toString();
					scrollTipView.setText(tips);
					scrollTipView.setVisibility(View.VISIBLE);

					if (task != null) {
						task.cancel();
					}
					if (timer != null) {
						timer.cancel();
					}

					task = new TimerTask() {
						public void run() {
							handler.sendEmptyMessage(HIDDEN_VIEW_TIPS);
						}
					};
					timer = new java.util.Timer(true);
					timer.schedule(task, timeToDelay);
				}
				break;
			case HIDDEN_VIEW_TIPS:
				if (scrollTipView != null) {
					scrollTipView.setVisibility(View.INVISIBLE);
				/*	LayoutParams layoutParams = indexBar.getLayoutParams();
					layoutParams.width = sideBarWidth;
					indexBar.setLayoutParams(layoutParams);*/
				}
				break;
			default:
				break;
			}
		}
	};
	private ArrayList<ContactBean> myContactNameLists; //排好序的联系人列表

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ListView list = (ListView) findViewById(R.id.myListView);
		list.setOnItemClickListener(this);
		ArrayList<ContactBean> contactBeanList = InitListViewData();
		ArrayList<String> stringList =new ArrayList<String>();
		for (int i = 0; i < contactBeanList.size(); i++) {
			stringList.add(contactBeanList.get(i).getDisplayName());
		}
		adapter = new ContactAdapter(this, contactBeanList,stringList);
		list.setAdapter(adapter);
		
		indexBar = (SideBar) findViewById(R.id.sideBar);
		sideBarWidth = indexBar.getLayoutParams().width;
		indexBar.setListView(list);
		indexBar.setHandler(handler);        //左边
		scrollTipView = (TextView) findViewById(R.id.tvScrollSectionShow);
		scrollTipView.setVisibility(View.INVISIBLE);
		addContactBtn = (Button) findViewById(R.id.addContactBtn);
		addContactBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Uri insertUri = android.provider.ContactsContract.Contacts.CONTENT_URI;
				Intent intent = new Intent(Intent.ACTION_INSERT, insertUri);
				startActivityForResult(intent, 1008);
			}
		});
	}

	private ArrayList<ContactBean> InitListViewData() {
		ArrayList<String> stringList = new ArrayList<String>();
		ContactInfoService servic = new ContactInfoService(ContactActivity.this);
		ArrayList<ContactBean> contactName = servic.getContact();
		for (int i = 0; i < contactName.size(); i++) {
			stringList.add(contactName.get(i).getDisplayName());
		}
		Collections.sort(stringList, new Comparator<String>() {
			@Override
			public int compare(String lhs, String rhs) {
				return StringUtil.cn2py(lhs).toUpperCase()
						.compareTo(StringUtil.cn2py(rhs).toUpperCase());
			}
		});
		 myContactNameLists = new ArrayList<ContactBean>();
        for (int i = 0; i < stringList.size(); i++) {
        	for (int j = 0; j < contactName.size(); j++) {
        		 String name = contactName.get(j).getDisplayName();
        		 if(name.equals(stringList.get(i))){
        			ContactBean bean = new ContactBean();
        			bean.setDisplayName(stringList.get(i));
        			bean.setPhoneNum(contactName.get(j).getPhoneNum());
        			bean.setContactId(contactName.get(j).getContactId());
        			myContactNameLists.add(bean);
        			contactName.remove(j);
        			break;
        		 }
			}
		}
		return myContactNameLists;
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		case R.id.myListView:
//			String contactPhoneNum = contactBeanList.get(arg2).getPhoneNum();
//            Intent intent = new Intent();
//            intent.setAction(intent.ACTION_CALL);
//            intent.setData(Uri.parse("tel:"+contactPhoneNum));
//            startActivity(intent);
			ContactBean cb = (ContactBean) myContactNameLists.get(arg2);
			showContactDialog(lianxiren1, cb, arg2);
			break;
		default:
			break;
		}
	}
	

	//群组联系人弹出页
	private void showContactDialog(final String[] arg ,final ContactBean cb, final int position){
		new AlertDialog.Builder(this).setTitle(cb.getDisplayName()).setItems(arg,
				new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which){

				Uri uri = null;

				switch(which){

				case 0://打电话
					String toPhone = cb.getPhoneNum();
					uri = Uri.parse("tel:" + toPhone);
					Intent it = new Intent(Intent.ACTION_CALL, uri);
					startActivity(it);
					break;

				case 1://发短息

					String threadId = getSMSThreadId(cb.getPhoneNum());
					Map<String, String> map = new HashMap<String, String>();
					map.put("phoneNumber", cb.getPhoneNum());
					map.put("threadId", threadId);
					BaseIntentUtil.intentSysDefault(ContactActivity.this, MessageBoxList.class, map);
					break;
				case 2:// 查看详细       修改联系人资料
					uri = ContactsContract.Contacts.CONTENT_URI;
					Uri personUri = ContentUris.withAppendedId(uri, cb.getContactId());
					Intent intent2 = new Intent();
					intent2.setAction(Intent.ACTION_VIEW);
					intent2.setData(personUri);
					startActivity(intent2);
					break;

				case 3:// 移动分组

					showDelete(cb.getContactId(), position);
					break;
					//					Intent intent3 = null;
					//					intent3 = new Intent();
					//					intent3.setClass(ContactHome.this, GroupChoose.class);
					//					intent3.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
					//					intent3.putExtra("联系人", contactsID);
					//					Log.e("contactsID", "contactsID---"+contactsID);
					//					ContactHome.this.startActivity(intent3);
				case 4:// 移出群组

					//					moveOutGroup(getRaw_contact_id(contactsID),Integer.parseInt(qzID));
					break;

				case 5:// 删除

					/*showDelete(cb.getContactId(), position);
					break;*/
				}
			}
		}).show();
	}

	// 删除联系人方法
	private void showDelete(final int contactsID, final int position) {
		new AlertDialog.Builder(this).setIcon(R.drawable.ic_launcher).setTitle("是否删除此联系人")
		.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				//源码删除
				Uri deleteUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactsID);
				Uri lookupUri = ContactsContract.Contacts.getLookupUri(ContactActivity.this.getContentResolver(), deleteUri);
				if(lookupUri != Uri.EMPTY){
					ContactActivity.this.getContentResolver().delete(deleteUri, null, null);
				}
				adapter.remove(position);
				adapter.notifyDataSetChanged();
				Toast.makeText(ContactActivity.this, "该联系人已经被删除.", Toast.LENGTH_SHORT).show();
			}
		})
		.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {

			}
		}).show();
	}
	
	public static String[] SMS_COLUMNS = new String[]{  
		"thread_id"
	};
	private String getSMSThreadId(String adddress){
		Cursor cursor = null;  
		ContentResolver contentResolver = getContentResolver();  
		cursor = contentResolver.query(Uri.parse("content://sms"), SMS_COLUMNS, " address like '%" + adddress + "%' ", null, null);  
		String threadId = "";
		if (cursor == null || cursor.getCount() > 0){
			cursor.moveToFirst();
			threadId = cursor.getString(0);
			cursor.close();
			return threadId;
		}else{
			cursor.close();
			return threadId;
		}
	}

}
