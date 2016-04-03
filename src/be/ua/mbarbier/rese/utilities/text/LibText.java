package be.ua.mbarbier.rese.utilities.text;

public class LibText {

	public static String concatenateStringArray(String[] s, String separator ) {
		String str = "";
		for (int i = 0; i < s.length; i++ ) {
			if (i < s.length-1) {
				str = str + s[i] + separator;
			} else {
				str = str + s[i];
			}
		}

		return str;
	} 
}
