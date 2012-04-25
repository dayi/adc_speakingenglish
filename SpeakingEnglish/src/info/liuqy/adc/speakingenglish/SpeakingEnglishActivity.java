package info.liuqy.adc.speakingenglish;

import java.util.Locale;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;

public class SpeakingEnglishActivity extends TabActivity {
	private TabHost mTabHost;
	private Intent mTab1Intent;
	private Intent mTab2Intent;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		this.mTab1Intent = new Intent(this, TabContentListActivity.class)
				.putExtra("type", "cn2en");

		this.mTab2Intent = new Intent(this, TabContentListActivity.class)
				.putExtra("type", "en2cn");

		// 取得tabHost对象
		mTabHost = getTabHost();

		/* 为TabHost添加标签 */
		mTabHost.addTab(mTabHost
				.newTabSpec("tabSpec1")
				.setIndicator(
						getResources().getString(R.string.speaking_english),
						null).setContent(this.mTab1Intent));
		mTabHost.addTab(mTabHost
				.newTabSpec("tabSpec2")
				.setIndicator(
						getResources().getString(R.string.speaking_chinese),
						null).setContent(this.mTab2Intent));

		int currentTab = this.getSharedPreferences("settings", MODE_PRIVATE)
				.getInt("currentTab", -1);
		System.out.println("type read:" + currentTab);
		if (currentTab != -1) {
			mTabHost.setCurrentTab(currentTab);
		} else {
			String lang = Locale.getDefault().getLanguage();
			if (Locale.CHINESE.equals(lang)) {
				mTabHost.setCurrentTab(0);
			} else if (Locale.ENGLISH.equals(lang)) {
				mTabHost.setCurrentTab(1);
			}
		}
	}

	@Override
	protected void onStop() {
		saveState();
		super.onStop();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		saveState();
		super.onSaveInstanceState(outState);
	}

	private void saveState() {
		this.getSharedPreferences("settings", MODE_PRIVATE).edit()
				.putInt("currentTab", mTabHost.getCurrentTab()).commit();
	}
}