package com.example.mama;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;

/*
 * singlton that handles the state of the activites and the state of the app 
 * 
 */
public class ActivityManager {
	private static Map<String, String> activites;

	public static final String LOGGER = "LOGGER";
	public static final String CONV = "CONV";
	public static final String ACTIVE = "active";
	public static final String STOPPED = "stopped";
	private static String[] a;
	public static String ACTIVE_CONTACT = null;

	public static ActivityManager manager = null;

	private ActivityManager() {
		activites = new HashMap<String, String>();
	}

	public static ActivityManager getInstance() {
		if (manager == null)
			manager = new ActivityManager();
		return manager;
	}

	/*
	 * 
	 */
	public void updateLogger(List<Conversation> cons) {
		a = new String[cons.size()];
		for (int i = 0; i < a.length; i++) {
			a[i] = cons.get(i).getSender().getName();
		}
	}

	public boolean isInLogger(String contact) {
		if (a != null) {
			for (int i = 0; i < a.length; i++) {
				if (a[i].equals(contact)) {
					return true;
				}
			}
		}
		return false;
	}

	/*
	 * update the state of where the application is in which activity info about
	 * activity and if in conversation with who ? and so
	 */
	public void UpdateState(String key, String state, String contact) {
		if ((state.equals(ACTIVE) || state.equals(STOPPED))
				&& (key.equals(LOGGER) || key.equals(CONV))) {
			if (activites.containsKey(key)) {
				activites.remove(key);

			}
			activites.put(key, state);
			if (key.equals(CONV) && state.equals(ACTIVE) && contact != null) {
				ACTIVE_CONTACT = contact;
			}
			if (key.equals(CONV) && state.equals(STOPPED) && contact != null) {
				ACTIVE_CONTACT = null;
			}

		}
	}

	/*
	 * for logging purpose
	 */
	public void getStatus() {
		if (!activites.isEmpty()) {
			if (activites.entrySet() != null) {
				for (Map.Entry<String, String> entry : activites.entrySet()) {
					if (ACTIVE_CONTACT != null)
						Log.d("State", entry.getKey() + " for "
								+ ACTIVE_CONTACT + " is " + entry.getValue());
					else
						Log.d("State",
								entry.getKey() + " is " + entry.getValue());

				}
			}
		}
	}

	public String getState(String act) {
		if (activites.containsKey(act)) {
			return activites.get(act);

		}
		return STOPPED;
	}

	public String activeUser() {
		return ACTIVE_CONTACT;
	}

}
