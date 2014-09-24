package xu.ye.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.TimerTask;

import xu.ye.R;
import xu.ye.bean.ContactBean;
import xu.ye.uitl.StringUtil;
import xu.ye.view.adapter.ContactAdapter;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

public class ContactActivity extends Activity implements OnItemClickListener{

	public static final int UPDATE_VIEW_TIPS = 1;
	public static final int HIDDEN_VIEW_TIPS = 2;

	private int sideBarWidth = 80;
	private long timeToDelay = 1500;
	private TextView scrollTipView;
	private SideBar indexBar;
	private java.util.Timer timer;
	private TimerTask task;

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
	private ArrayList<ContactBean> contactBeanList;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ListView list = (ListView) findViewById(R.id.myListView);
		list.setOnItemClickListener(this);
		contactBeanList = InitListViewData();
		ArrayList<String> stringList =new ArrayList<String>();
		for (int i = 0; i < contactBeanList.size(); i++) {
			stringList.add(contactBeanList.get(i).getDisplayName());
		}
		ContactAdapter adapter = new ContactAdapter(this, contactBeanList,stringList);
		list.setAdapter(adapter);
		
		indexBar = (SideBar) findViewById(R.id.sideBar);
		sideBarWidth = indexBar.getLayoutParams().width;
		indexBar.setListView(list);
		indexBar.setHandler(handler);        //左边
		scrollTipView = (TextView) findViewById(R.id.tvScrollSectionShow);
		scrollTipView.setVisibility(View.INVISIBLE);
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
		ArrayList<ContactBean> myContactNameLists = new ArrayList<ContactBean>();
        for (int i = 0; i < stringList.size(); i++) {
        	for (int j = 0; j < contactName.size(); j++) {
        		 String name = contactName.get(j).getDisplayName();
        		 if(name.equals(stringList.get(i))){
        			ContactBean bean = new ContactBean();
        			bean.setDisplayName(stringList.get(i));
        			bean.setPhoneNum(contactName.get(j).getPhoneNum());
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
			String contactPhoneNum = contactBeanList.get(arg2).getPhoneNum();
            Intent intent = new Intent();
            intent.setAction(intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:"+contactPhoneNum));
            //��ʼ
            startActivity(intent);
			break;

		default:
			break;
		}
		
	}

}
