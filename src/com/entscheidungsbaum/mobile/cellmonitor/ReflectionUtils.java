package com.entscheidungsbaum.mobile.cellmonitor;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import android.util.Log;

/**
 * @author marcus@entscheidungsbaum.com 05.07.2013
 * 
 *         ReflectionUtils.java which reflects all public members to be provided
 */
public final class ReflectionUtils {

	private static final String LOG_TAG = ReflectionUtils.class.getSimpleName();

	static List<String> objectInherited = new ArrayList<String>();

	public static final HashMap<String, Object> dumpClassAsStringFeed(
			Class<?> mClass, Object mInstance) {

		// setup up regexp token to get rid of useless fields
		objectInherited.add("hashCode");
		objectInherited.add("wait");
		objectInherited.add("toString");
		objectInherited.add("CREATOR");
		objectInherited.add("SignalStrength");
		objectInherited.add("notify");
		objectInherited.add("notifyAll");
		objectInherited.add("getClass");
		objectInherited.add("equals");
		objectInherited.add("describeContents");
		objectInherited.add("writeToParcel");
		objectInherited.add("fillInNotifierBundle");
		objectInherited.add("newFromBundle");
		objectInherited.add("hashCode");
		String patternString = "\\b(" + StringUtils.join(objectInherited, "|")
				+ ")\\b";
		// Pattern pattern = Pattern.compile(patternString);
		Pattern pattern = Pattern
		// .compile("CREATOR|wait|LOG_TAG|DBG|notifyAll|notify|newFromBundle|getClass");
				.compile(patternString);

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
			Log.d(LOG_TAG,
					"MFIELD pattern =  field getName " + mField.getName());
			Matcher matcher = pattern.matcher(mField.getName());
			if (matcher.find()) {
				try {

					// if (matcher.matches()) {
					Log.d(LOG_TAG,
							"useless MFIELD  FOUND " + matcher.toString()
									+ " pattern matcher = " + pattern.pattern());

					mStr += "";

				} catch (Exception e) {

					Log.e(LOG_TAG, "Could not get Field " + mField.getName()
							+ " ", e);
				}

			} else {

				mStr += mField.getName();
				Log.d(LOG_TAG, "useful MSTR found and added");
			}
		}
		// here we invoke the method which is in the array.
		// get rid of the object methods and superclass methods !!! @TODO
		final Method[] relevantMethod = mClass.getMethods();

		for (int i = 0; i <= relevantMethod.length - 1; i++) {
			String logMethod = relevantMethod[i].getName();
			if (logMethod.matches(patternString)) {

				Log.d(LOG_TAG, " IRRELEVANTMETHODS -> " + logMethod
						+ " Length ->  " + relevantMethod.length);
			} else {

				relevantMethod[i].setAccessible(true);
				mStr += relevantMethod[i].getReturnType() + "="
						+ relevantMethod[i].getReturnType();

				try {
					Object mRet = relevantMethod[i].invoke(mInstance);
					mStr += relevantMethod[i].invoke(mInstance).toString();
					Log.d(LOG_TAG,
							"INVOCATION METHOD " + relevantMethod[i].getName()
									+ " for instance " + mInstance
									+ "\n MSTR => " + mStr + "\n");
					// final Object mRet = myMethods.invoke(mInstance);
					Log.d(LOG_TAG,
							" MYRELEVANTMETHODS -> "
									+ relevantMethod[i].getName() + " RETURN "
									+ mRet);
					mTokenList.put(relevantMethod[i].getName(), mRet.toString());
					Log.d(LOG_TAG, " MTOKENLIST = " + mTokenList);
				} catch (IllegalArgumentException e1) {
					// TODO Auto-generated catch block
					Log.e(LOG_TAG, "IllegalArgumentException " + e1);
				} catch (IllegalAccessException e1) {
					// TODO Auto-generated catch block
					Log.e(LOG_TAG, "IllegalAccessException " + e1);
				} catch (InvocationTargetException e1) {
					// TODO Auto-generated catch block
					Log.e(LOG_TAG, "InvocationTargetException " + e1);
				}
				// finally {
				// sb.append(mStr);
				// mTokenList.put(relevantMethod[i].getName(), mStr);
				// }
				sb.append(mStr);

			}

		}

		return mTokenList;
	}

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
