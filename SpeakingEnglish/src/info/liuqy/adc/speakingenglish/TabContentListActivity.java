package info.liuqy.adc.speakingenglish;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

public class TabContentListActivity extends ListActivity {

	List<String> cns = new ArrayList<String>();
	ArrayAdapter<String> adapter;

	Map<String, String> exprs = null;
	Map<String, String> exprspy = null;
	private String type;

	private class DFA {
		private static final int INIT_STATE = 0, EXPR_TAG = 1, CN_TAG = 2,
				EN_TAG = 3, PY_TAG = 4, CN_TEXT = 5, EN_TEXT = 6, PY_TEXT = 7,
				PRE_FINAL = 8, FINAL_STATE = 9;
		int currentState = 0;
		Map<Integer, Map<String, Integer>> T = new HashMap<Integer, Map<String, Integer>>();

		public DFA() {
			Map<String, Integer> m = new HashMap<String, Integer>();
			m.put("expression", EXPR_TAG);
			T.put(INIT_STATE, m);
			m = new HashMap<String, Integer>();
			m.put("cn", CN_TAG);
			m.put("en", EN_TAG);
			m.put("py", PY_TAG);

			T.put(EXPR_TAG, m);

			m = new HashMap<String, Integer>();
			m.put("text", CN_TEXT);
			T.put(CN_TAG, m);

			m = new HashMap<String, Integer>();
			m.put("text", EN_TEXT);
			T.put(EN_TAG, m);

			m = new HashMap<String, Integer>();
			m.put("text", PY_TEXT);
			T.put(PY_TAG, m);

			m = new HashMap<String, Integer>();
			m.put("py", FINAL_STATE);
			T.put(EN_TEXT, m);

			m = new HashMap<String, Integer>();
			m.put("en", EN_TAG);
			T.put(CN_TEXT, m);

			m = new HashMap<String, Integer>();
			m.put("text", FINAL_STATE);
			T.put(PRE_FINAL, m);
		}

		public void reset() {
			currentState = 0;
		}

		public int nextState(String symbol) {
			if (currentState != FINAL_STATE
					&& T.get(currentState).containsKey(symbol))
				currentState = T.get(currentState).get(symbol);
			return currentState;
		}
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tabcontent);

		type = this.getIntent().getExtras().getString("type");

		try {
			exprs = loadExpressionsFromXml(R.xml.cn2en, type);
		} catch (IOException e) {
			Toast.makeText(this, R.string.error_xml_file, Toast.LENGTH_SHORT);
		} catch (XmlPullParserException e) {
			Toast.makeText(this, R.string.error_parsing_xml, Toast.LENGTH_SHORT);
		}

		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, cns);

		this.setListAdapter(adapter);

		for (String cn : exprs.keySet()) {
			cns.add(cn);
		}
		// adapter.notifyDataSetChanged();
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		TextView tv = (TextView) v;
		String text = tv.getText().toString();
		if (exprs.containsKey(text)) // Chinese displayed now
			tv.setText(exprs.get(text));
		else
			// English displayed now, refresh the display
			adapter.notifyDataSetChanged();
	}

	private Map<String, String> loadExpressionsFromXml(int resourceId,
			String type) throws XmlPullParserException, IOException {
		Map<String, String> exprs = new HashMap<String, String>();
		XmlPullParser xpp = getResources().getXml(resourceId);
		DFA dfa = new DFA();
		String cn = null, en = null, py = null;
		while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
			if (xpp.getEventType() == XmlPullParser.START_TAG) {
				dfa.nextState(xpp.getName());
			} else if (xpp.getEventType() == XmlPullParser.TEXT) {
				int state = dfa.nextState("text");
				if (state == DFA.CN_TEXT) {
					cn = xpp.getText();
				} else if (state == DFA.EN_TEXT) {
					en = xpp.getText();
				} else if (state == DFA.PY_TEXT) {
					py = xpp.getText();
				} else if (state == DFA.FINAL_STATE) {
					if (cn == null)
						cn = xpp.getText();
					else if (en == null)
						en = xpp.getText();
					else if (py == null)
						py = xpp.getText();

					if (type.equals("cn2en")) {
						exprs.put(cn, en);
					} else {
						exprs.put(en, cn + "\n" + py);
					}

					dfa.reset();
					cn = en = py = null;
				}
			}
			xpp.next();
		}
		return exprs;
	}

}
