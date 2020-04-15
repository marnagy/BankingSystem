//import java.time.LocalDate;
//import java.time.Month;
//import java.time.ZonedDateTime;
//
//public class MonthYear {
//	public final Month month;
//	public final int year;
//	public MonthYear(int month, int year) throws InvalidFormatException {
//		if ( !(month > 0 && month <= 12 && year >= 2020 ) ){
//			throw new InvalidFormatException("Invalid format");
//		}
//		else{
//			this.year = year;
//			this.month = Month.of(month);
//		}
//	}
//	public MonthYear(ZonedDateTime datetime) {
//		this.year = datetime.getYear();
//		this.month = datetime.getMonth();
//	}
//	public MonthYear(LocalDate date) {
//		this.year = date.getYear();
//		this.month = date.getMonth();
//	}
//	public MonthYear getOneSooner(){
//		try{
//			if (month != Month.JANUARY){
//				return new MonthYear(this.month.getValue() - 1, this.year);
//			}
//			else{ // december of previous year
//				return new MonthYear(12, this.year - 1);
//			}
//		}
//		catch (InvalidFormatException e){
//			// THIS SHOULD NEVER HAPPEN !!!
//			return null;
//		}
//	}
//	public MonthYear getOneLater(){
//		try{
//			if (month != Month.DECEMBER){
//				return new MonthYear(this.month.getValue() + 1, this.year);
//			}
//			else{ // december of previous year
//				return new MonthYear(1, this.year + 1);
//			}
//		}
//		catch (InvalidFormatException e){
//			// THIS SHOULD NEVER HAPPEN !!!
//			return null;
//		}
//	}
//	@Override
//	public String toString(){
//		return String.format("%02d-%02d", month.getValue(), year);
//	}
//	@Override
//	public boolean equals(Object o){
//		if (o.getClass() != this.getClass()){
//			return false;
//		}
//		else{
//			MonthYear other = (MonthYear)o;
//			return other.year == this.year && other.month.getValue() == this.month.getValue();
//		}
//	}
//}
