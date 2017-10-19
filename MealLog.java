import java.util.Scanner;
import java.sql.*;
import java.util.Arrays;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.Date;

public class MealLog
{

	public Connection db;
	private Statement stmt;
	private ResultSet rs;

	public MealLog()
	{
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
			db = DriverManager.getConnection("jdbc:mysql://localhost:3306/meal_log", "root", "");
			stmt = db.createStatement();
		}
		catch(Exception ex)
		{
			System.out.println("DB Error: " + ex);
		}
			
	}

	private void getWhys()
	{
		String query = "SELECT why FROM whys ORDER BY why DESC";
		System.out.println("Reasons to lose weight\n");
			
		try
		{
			rs = stmt.executeQuery(query);
			while(rs.next())
				System.out.println(rs.getString(1));

			rs.close();

		}  
		catch(Exception ex)
		{
			System.out.println("DB Error: " + ex);
		}
			


	}

	private void menu()
	{
		String[] menu = {
			"1. Close application",
			"2. View my whys",
			"3. View menu",
			"4. Log a meal"
		};

		for(int i = 0; i < menu.length; i++)
			System.out.println(menu[i]);
	}

	private void logMeal()
	{
		System.out.println("\nEnter meal period, pass/fail, & date (separated by commas):\n");

		String[] meals = {"breakfast", "lunch", "dinner"};
		String[] result = {"pass", "fail"};

		Scanner mealRead = new Scanner(System.in);
		String mealValue = mealRead.nextLine();

		String[] ent = mealValue.split(",");

		if(ent.length == 3){
			String meal = ent[0].trim();
			String pass = ent[1].trim();
			String date = ent[2].trim();

			DateFormat df = new SimpleDateFormat("YYYY-mm-dd");
			Date pDate;
			boolean mealValidated = true;
			boolean passValidated = true;

			if(!Arrays.asList(meals).contains(meal))
				mealValidated = false;
			if(!Arrays.asList(result).contains(pass))
				passValidated = false;

			if(mealValidated && passValidated)
			{
				try{
					pDate = df.parse(date);
					String saveDate = df.format(pDate);
					System.out.println("Entered: meal " + meal + " result: " + pass + " date: " + saveDate);
				}
				catch(ParseException e){
					System.out.println("Date validation failed: " + e);
				}	
			}
			else
			{
				if(!mealValidated)
					System.out.println(meal + " is not a valid meal period");
				if(!passValidated)
					System.out.println(pass + " is not a valid pass/fail option");
			}

		}
		else
			System.out.println("Error: 3 values must be entered");

	}

	public static void main(String[] args)
	{
		MealLog m = new MealLog();
		Scanner read = new Scanner(System.in);

		boolean stop = false;

		m.menu();

		while(stop == false)
		{
			System.out.println("\nSelect an option\n");
			String value = read.next();

			if(value != "")
			{
				try
				{
					
					int entered = Integer.parseInt(value);

					switch(entered)
					{
						case 1:
							System.out.println("Exiting Meal Log");
							stop = true;
							break;
						case 2:
							m.getWhys();
							break;
						case 3:
							m.menu();
							break;
						case 4:
							m.logMeal();
							break;
						default:
							System.out.println("Not a valid entry");
							break;
					}
				}
				catch(NumberFormatException e)
				{
					System.out.println(value + " not a valid integer: " + e);
				}
			}
		}
		
		try{
			m.db.close();
		}
		catch(Exception ex){
			System.out.println("DB Error: " + ex);
		}
		
		System.exit(0);	
	}
}