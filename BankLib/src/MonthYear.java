import java.time.Month;
import java.time.ZonedDateTime;

public class MonthYear {
	public final Month month;
	public final int year;
	public MonthYear(int month, int year) throws InvalidFormatException {
		if ( !(month > 0 && month <= 12 && year >= 2020 ) ){
			throw new InvalidFormatException("Invalid format");
		}
		else{
			this.year = year;
			this.month = Month.of(month);
		}
	}
	public MonthYear(ZonedDateTime datetime) {
		this.year = datetime.getYear();
		this.month = datetime.getMonth();
	}
}
