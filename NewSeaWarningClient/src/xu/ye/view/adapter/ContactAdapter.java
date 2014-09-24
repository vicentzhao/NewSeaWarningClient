package xu.ye.view.adapter;

import java.util.ArrayList;
import java.util.SortedSet;
import java.util.TreeSet;

import xu.ye.R;
import xu.ye.bean.ContactBean;
import xu.ye.uitl.StringUtil;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

public class ContactAdapter extends BaseAdapter implements SectionIndexer {
	private ArrayList<String> stringArray;
	private Context context;
	private ArrayList<ContactBean> beans;

	public char[] getSectionArray() {

		SortedSet<Character> sections = new TreeSet<Character>();
		for (String str : stringArray) {
			sections.add(StringUtil.cn2py(str).toUpperCase().charAt(0));
		}
		char[] sectionArray = new char[sections.size()];
		int i = 0;
		for (Character c : sections) {
			sectionArray[i++] = c;
		}
		return sectionArray;
	}

	public ContactAdapter(Context _context, ArrayList<ContactBean> contactbeans,ArrayList<String> stringArray) {
		this.beans = contactbeans;
		this.stringArray=stringArray;
		context = _context;
	}

	public int getCount() {
		return stringArray.size();
	}

	public Object getItem(int arg0) {
		return stringArray.get(arg0);
	}

	public long getItemId(int arg0) {
		return 0;
	}

	public View getView(int position, View v, ViewGroup parent) {
		LayoutInflater inflate = ((Activity) context).getLayoutInflater();
		View view = (View) inflate.inflate(R.layout.listview_row, null);
		LinearLayout header = (LinearLayout) view.findViewById(R.id.section);
		String label = stringArray.get(position);
		char firstChar = StringUtil.cn2py(label).toUpperCase().charAt(0);
		if (position == 0) {
			setSection(header, label);
		} else {
			String preLabel = stringArray.get(position - 1);
			char preFirstChar = StringUtil.cn2py(preLabel).toUpperCase()
					.charAt(0);
			if (firstChar != preFirstChar) {
				setSection(header, label);
			} else {
				header.setVisibility(View.GONE);
			}
		}
		TextView textView = (TextView) view.findViewById(R.id.textView);
		TextView tv_phonenum = (TextView) view.findViewById(R.id.phonenum);
		tv_phonenum.setText(beans.get(position).getPhoneNum());
		textView.setText(label);
		return view;
	}

	private void setSection(LinearLayout header, String label) {
		TextView text = new TextView(context);
		header.setBackgroundColor(0xffaabbcc);
		text.setTextColor(Color.WHITE);
		label = StringUtil.cn2py(label).toUpperCase();
		text.setText(label.substring(0, 1));
		text.setTextSize(20);
		text.setPadding(5, 0, 0, 0);
		text.setGravity(Gravity.CENTER_VERTICAL);
		header.addView(text);
	}

	public int getPositionForSection(int section) {
		if (section == 35) {
			return 0;
		}
		for (int i = 0; i < stringArray.size(); i++) {
			String l = stringArray.get(i);
			char firstChar = StringUtil.cn2py(l).toUpperCase().charAt(0);
			if (firstChar == section) {
				return i;
			}
		}
		return -1;
	}

	public int getSectionForPosition(int arg0) {
		return 0;
	}

	public Object[] getSections() {
		return null;
	}
}
