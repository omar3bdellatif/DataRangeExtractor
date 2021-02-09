import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class Main {
	
	 static boolean isLeapYear(int year)
	    {
	        
	        if (year % 400 == 0)
	            return true;
	     
	        
	        if (year % 100 == 0)
	            return false;
	     
	        
	        if (year % 4 == 0)
	            return true;
	        return false;
	    }
	
	public static String getNextDay(String dateString) throws ParseException
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Calendar c = Calendar.getInstance();
		c.setTime(sdf.parse(dateString));
		c.add(Calendar.DATE, 1);
		String result = sdf.format(c.getTime()); 
		return result;
	}
	
	
	public static ArrayList<String> interpolateDates(JSONArray innerJsonArray) throws JSONException, ParseException
	{
		ArrayList<String> interpolatedList = new ArrayList<String>();
		String dateAString = (String) innerJsonArray.get(0);
		String dateBString = (String) innerJsonArray.get(1);
		interpolatedList.add(dateAString);
		
		String currentDate = dateAString;
		while(true)
		{
			currentDate = getNextDay(currentDate);
			if(currentDate.equals(dateBString))
			{
				break;
			}
			interpolatedList.add(currentDate);
		}
		interpolatedList.add(dateBString);
		return interpolatedList;
	}
	public static boolean consecutiveDates(int dayA,int monthA, int yearA,int dayB, int monthB,int yearB)
	{
		

		if(yearA == yearB && monthA == monthB && dayA+1 == dayB)
		{
			return true;
		}
		else if(yearA == yearB && monthA+1 == monthB)
		{
			
			if(monthA == 1 || monthA == 3 || monthA == 5 || monthA == 7 || monthA == 8 || monthA == 10 || monthA == 12)
			{
				
				if(dayB == 1 && dayA == 31)
				{
					return true;
				}
			}
			else if(monthA == 2)
			{
				int numOfDays = 28;
				if(isLeapYear(yearA))
				{
					numOfDays = 29;
				}
				
				if(dayA == numOfDays && dayB == 1)
				{
					return true;
				}

			}
			else
			{
				if(dayB == 1 && dayA == 30)
				{
					return true;
				}
			}
			
		}
		else if(yearA + 1 == yearB && monthA == 12 && monthB == 1)
		{
			if(dayA == 31 && dayB == 1)
			{
				return true;
			}
		}
		return false;
	}
	
	public static ArrayList<Integer> parseDate(String dateString) throws ParseException
	{
		Date date=new SimpleDateFormat("yyyy-MM-dd").parse(dateString);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int month = calendar.get(Calendar.MONTH)+1;
		int year = calendar.get(Calendar.YEAR);
		
		ArrayList<Integer> result = new ArrayList<Integer>();
		result.add(day);
		result.add(month);
		result.add(year);
		return result;
	}
	
	public static JSONObject scrambledToOrdered(JSONObject datesJson) throws ParseException, JSONException
	{
		
		JSONArray jsonArray = datesJson.getJSONArray("days");
		ArrayList<String> dates = new ArrayList<String>();
		for(int i = 0; i < jsonArray.length(); i++){
		     dates.add(jsonArray.getString(i));
		}
		
		ArrayList<ArrayList<String>>result = new ArrayList<ArrayList<String>>();
		boolean consecutive = false;
		ArrayList<String> currentResult = new ArrayList<String>();
		for(int i=1;i<dates.size();i++)
		{
			

			String dateString = dates.get(i);
			ArrayList<Integer> parsedDate = parseDate(dateString);
			int dayB = parsedDate.get(0);
			int monthB = parsedDate.get(1);
			int yearB = parsedDate.get(2);
			
			dateString = dates.get(i-1);
			parsedDate = parseDate(dateString);
			int dayA = parsedDate.get(0);
			int monthA = parsedDate.get(1);
			int yearA = parsedDate.get(2);
			
			if(!consecutive)
			{
				currentResult.add(dateString);
			}
			
			consecutive = consecutiveDates(dayA,monthA, yearA,dayB, monthB,yearB);
			if(!consecutive)
			{
				if(currentResult.get(0) != dateString)
				{
					currentResult.add(dateString);
				}
				//push to result
				result.add(currentResult);
				currentResult = new ArrayList<String>();
			}
			
			if(i == dates.size()-1)
			{
				dateString = dates.get(i);
				currentResult.add(dateString);
				result.add(currentResult);
				currentResult = new ArrayList<String>();

			}
			

		}
		JSONObject resultJson = new JSONObject();
		resultJson.put("date_ranges", result);
		return resultJson;
		
		
	}
	
	public static JSONObject orderedToScrambled(JSONObject datesJson) throws ParseException, JSONException
	{
		ArrayList<String> results = new ArrayList<String>();
		JSONArray jsonArray = datesJson.getJSONArray("date_ranges");
		for(int i = 0; i < jsonArray.length(); i++){
			JSONArray innerJsonArray = jsonArray.getJSONArray(i);
			if(innerJsonArray.length() == 2)
			{
				ArrayList<String> innerArray = interpolateDates(innerJsonArray);
				for(int j = 0; j < innerArray.size(); j++) 
				{
					results.add(innerArray.get(j));
				}
			}
			else
			{
				for(int j = 0; j < innerJsonArray.length(); j++) 
				{
				results.add((String) innerJsonArray.get(j));
				}
			}
			
			
			
		}
		JSONObject resultJson = new JSONObject();
		resultJson.put("days", results);
		return resultJson;
		
	}
	public static void main(String[] args)
	{
		
	
		
		JSONObject jsonObject;
		try {
			
			jsonObject = new JSONObject("{days:['2021-02-27','2021-02-28','2021-03-01','2020-09-24','2020-09-25','2020-09-26','2020-12-17','2021-02-03','2021-02-05','2021-02-06']}");
			JSONObject result = scrambledToOrdered(jsonObject);
			System.out.println(result);
			
			jsonObject = new JSONObject("{\"date_ranges\":[[\"2019-09-23\"],[\"2020-09-23\",\"2020-09-26\"],[\"2020-12-17\"],[\"2021-02-03\"],[\"2021-02-05\",\"2021-02-06\"]]}");
			result = orderedToScrambled(jsonObject);
			System.out.println(result);			
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				

		
	}
}
