package com.entscheidungsbaum.mobile.cellmonitor;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import android.util.Log;

/**
 * @author marcus@entscheidungsbaum.com 05.07.2013
 * 
 *         ReflectionUtils.java which reflects all public members to be provided
 */
public final class ReflectionUtils {

	public static final String LOG_TAG = ReflectionUtils.class.getSimpleName();

	static List<String> objectInherited = new ArrayList<String>();

	public static final HashMap<String, Object> dumpClassAsStringFeed(
			Class<?> mClass, Object mInstance) {

		//setup up regexp token to get rid of useless fields
//		objectInherited.add("hashCode");
//		objectInherited.add("wait");
//		objectInherited.add("toString");
//		objectInherited.add("CREATOR");
//		objectInherited.add("SignalStrength");
//		objectInherited.add("notify");
//		objectInherited.add("newFromBundle");
//		objectInherited.add("hashCode");
//		String patternString = "\\b(" + StringUtils.join(objectInherited, "|") + ")\\b";
//		Pattern pattern = Pattern.compile("");
		
		StringBuilder sb = new StringBuilder();
		HashMap<String, Object> mTokenList = new HashMap<String, Object>();

		if (mClass == null || mInstance == null)
			return null;
		String mStr = mClass.getSimpleName();// + "\n\n";

		// mStr += "";

		final Field[] mFields = mClass.getDeclaredFields();

		for (final Field mField : mFields) {
			mField.setAccessible(true);
			// mStr += mField.getName() + " (" + mField.getType() + ") = ";
			mStr += mField.getName();

			try {
				Log.d(LOG_TAG, "mFields = " + mField.getName() + " value = "
						+ mField.get(mInstance).toString());

				if (mStr.trim().contains("CREATOR")
						
						|| (mStr.trim().contains("hashCode")
								|| (mStr.trim().contains("SignalStrength")

								|| mStr.trim().contains("getClass")
								|| mStr.trim().contains("notifyAll")
								|| mStr.trim().contains("newFromBundle")
								|| mStr.trim().contains("CREATOR")
								|| mStr.trim().contains("notify") || mStr.trim()
									.contains("toString")))) {

					Log.d(LOG_TAG, "CREATOR FOUND mSTR  charseq found " + mStr);
					mStr += "";
				}
				else {
					mStr += mField.get(mInstance).toString();
					Log.d(LOG_TAG, "mStr of mFields => " + mStr );
				}

			} catch (Exception e) {

				Log.e(LOG_TAG, "Could not get Field " + mField.getName() + " ",
						e);
			}

		}

		final Method[] mMethods = mClass.getMethods();

		for (final Method mMethod : mMethods) {
			mMethod.setAccessible(true);

			mStr += mMethod.getReturnType() + " " + mMethod.getName();

			sb.append(mStr);

			try {
				final Object mRet = mMethod.invoke(mInstance);

				Log.d(LOG_TAG,
						" METHOD mMethod.getName()= " + mMethod.getName()
								+ " MSTR in Reflection = " + mRet + " \n");
				if (mMethod.getName().trim().contains("hashCode")
						|| mMethod.getName().trim().contains("getClass")
						|| mMethod.getName().trim().contains("notifyAll")
						|| mMethod.getName().trim().contains("newFromBundle")
						|| mMethod.getName().trim().contains("CREATOR")
						|| mMethod.getName().trim().contains("notify")
						|| mMethod.getName().trim().contains("toString")) {

					mStr += "";
					Log.d(LOG_TAG, "FOUND NOT RELEVANT !!! get rid of it "
							+ "key" + mStr + " value = " + mRet + "\n");
				} else {
					
					mStr += (mRet == null) ? "" : mMethod.invoke(mInstance)
							.toString();
					Log.d(LOG_TAG, "FOUND RELEVANT !!! get rid of it "
							+ "key" + mStr + " value = " + mRet + "\n");
					mTokenList.put(mMethod.getName().trim(), mRet);
				}

			} catch (Exception e) {
				mStr += "null";
				mTokenList.put(mMethod.getName(), mStr);
				Log.e(LOG_TAG, "Could not get Method `" + mMethod.getName()
						+ "`.", e);
			}
			Log.d(LOG_TAG, " MTOKENLIST = " + mTokenList);
		}

		return mTokenList;
	}

	public static final void dumpMethodToJson() {
	}

	/**
	 * Dumps a {@link Class}'s {@link Method}s and {@link Field}s as a String.
	 */
	/*
	 * public static final HashMap<String, Object> dumpClass(Class<?> mClass,
	 * Object mInstance) { if (mClass == null || mInstance == null) return null;
	 * 
	 * String mStr = mClass.getSimpleName() + "\n\n"; HashMap<String, Object>
	 * mTokenList = new HashMap<String, Object>();
	 * 
	 * mStr += "FIELDS\n\n";
	 * 
	 * final Field[] mFields = mClass.getDeclaredFields();
	 * 
	 * for (final Field mField : mFields) { mField.setAccessible(true);
	 * 
	 * mStr += mField.getName() + " (" + mField.getType() + ") = ";
	 * 
	 * try { mStr += mField.get(mInstance).toString(); } catch (Exception e) {
	 * mStr += "null"; Log.e(LOG_TAG, "Could not get Field `" + mField.getName()
	 * + "`.", e); }
	 * 
	 * mStr += "\n"; }
	 * 
	 * mStr += "METHODS\\nn";
	 * 
	 * // Dump all methods.
	 * 
	 * final Method[] mMethods = mClass.getMethods();
	 * 
	 * for (final Method mMethod : mMethods) { mMethod.setAccessible(true);
	 * 
	 * mStr += mMethod.getReturnType() + " " + mMethod.getName() + "() = ";
	 * 
	 * try { final Object mRet = mMethod.invoke(mInstance); mStr += (mRet ==
	 * null) ? "null" : mMethod.invoke(mInstance) .toString(); Log.d(LOG_TAG,
	 * " METHOD " + mMethod.getName()); if (mMethod.getName() == "hashCode") {
	 * Log.d(LOG_TAG, "FOUND GETCLASS !!! get rid of it "); }
	 * mTokenList.put(mMethod.getName(), mRet);
	 * 
	 * } catch (Exception e) { mStr += "null"; mTokenList.put(mMethod.getName(),
	 * mStr);
	 * 
	 * Log.e(LOG_TAG, "Could not get Method `" + mMethod.getName() + "`.", e); }
	 * 
	 * // mStr += "\n"; }
	 * 
	 * return mTokenList; }
	 */
	// 07-16 10:48:58.810:
	// D/com.entscheidungsbaum.mobile.cellmonitor.CellMonitorActivatorService(31836):
	// CREATORandroid.telephony.SignalStrength$1@40d76970DBGfalseLOG_TAGSignalStrengthNUM_SIGNAL_STRENGTH_BINS5SIGNAL_STRENGTH_GOOD3SIGNAL_STRENGTH_GREAT4SIGNAL_STRENGTH_MODERATE2SIGNAL_STRENGTH_NAMES[Ljava.lang.String;@40d76798SIGNAL_STRENGTH_NONE_OR_UNKNOWN0SIGNAL_STRENGTH_POOR1isGsmtruemCdmaDbm-1mCdmaEcio-1mEvdoDbm-1mEvdoEcio-1mEvdoSnr-1mGsmBitErrorRate-1mGsmSignalStrength29mLteCqi-1mLteRsrp-1mLteRsrq-1mLteRssnr-1mLteSignalStrength-1int
	// describeContents() = 0boolean equals() = nullvoid fillInNotifierBundle()
	// = nullint getAsuLevel() = 29int getCdmaAsuLevel() = 16int getCdmaDbm() =
	// -1int getCdmaEcio() = -1int getCdmaLevel() = 4
	// 07-16 10:48:59.200:
	// D/com.entscheidungsbaum.mobile.cellmonitor.NetworkingTools(31836):
	// COMPLETE PING STRING => [Ljava.lang.String;@419c7fe8

	/**
	 * not in use so far
	 * 
	 * @return A string containing the values of all static {@link Field}s.
	 */
	public static final HashMap<String, Object> dumpStaticFields(
			Class<?> mClass, Object mInstance) {

		HashMap<String, Object> mTokenList = new HashMap<String, Object>();

		if (mClass == null || mInstance == null)
			return null;

		String mStr = mClass.getSimpleName() + "\n\n";

		mStr += "STATIC FIELDS\n\n";

		final Field[] mFields = mClass.getDeclaredFields();

		for (final Field mField : mFields) {
			if (ReflectionUtils.isStatic(mField)) {
				mField.setAccessible(true);

				mStr += mField.getName() + " (" + mField.getType() + ") = ";
				mTokenList.put(mField.getName(), mField.getType());
				try {
					mStr += mField.get(mInstance).toString();
				} catch (Exception e) {
					mStr += "null";
					mTokenList.put("", "");
					Log.e(LOG_TAG, "Could not get Field `" + mField.getName()
							+ "`.", e);
				}

				mStr += "\n";
			}
		}
		Log.d(LOG_TAG, " MTOKENLIST IN DUMPSTATIC FIELDS " + mTokenList);
		// return mStr;
		return mTokenList;
	}

	/**
	 * @return True if the {@link Field} is static.
	 */
	final static boolean isStatic(Field field) {
		final int modifiers = field.getModifiers();
		return (Modifier.isStatic(modifiers));
	}
}
