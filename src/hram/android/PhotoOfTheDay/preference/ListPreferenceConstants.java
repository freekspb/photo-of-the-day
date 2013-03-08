package hram.android.PhotoOfTheDay.preference;

public class ListPreferenceConstants {
	/**
	 * Делитель по умолчанию, из исходников. 
	 */
	public static final String DEFAULT_SEPARATOR = "OV=I=XseparatorX=I=VO";
	
	/**
	 * Делитель заданный в настройках.
	 * Задан здесь, потому что необходимо использовать его в WidgetBroadcastReceiver,
	 * а к атрибутам настроек не достучаться оттуда.
	 */
	public static final String SEPARATOR = "X";
	
	/**
	 * Флаг выделенности всех элементов. 
	 */
	public static final String CHECK_ALL = "CheckAll";
}
