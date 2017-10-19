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
		catch(Exception e)
		{
			System.out.println("DB Error: " + e);
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
		catch(SQLException e)
		{
			System.out.println("DB Error: " + e);
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
		System.out.println("\nEnter meal period, pass/fail, sugar, & date (separated by commas):\n");

		String[] meals = {"breakfast", "lunch", "dinner"};
		String[] result = {"pass", "fail"};
		String[] sugar = {"yes", "no"};

		Scanner mealRead = new Scanner(System.in);
		String mealValue = mealRead.nextLine();

		String[] ent = mealValue.split(",");

		if(ent.length == 4){
			String meal = ent[0].trim();
			String pass = ent[1].trim();
			String sugars = ent[2].trim();
			String date = ent[3].trim();

			DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			Date pDate;
			boolean mealValidated = true;
			boolean passValidated = true;
			boolean sugarsValidated = true;

			if(!Arrays.asList(meals).contains(meal.toLowerCase()))
				mealValidated = false;
			if(!Arrays.asList(result).contains(pass.toLowerCase()))
				passValidated = false;
			if(!Arrays.asList(sugar).contains(sugars.toLowerCase()))
				sugarsValidated = false;

			if(mealValidated && passValidated && sugarsValidated)
			{
				try{
					pDate = df.parse(date);
					String saveDate = df.format(pDate);
					//System.out.println("Entered: meal " + meal + " result: " + pass + " sugars: " + sugars + " date: " + saveDate);

					try{
						String query = "INSERT INTO meals (time, result, sugar, date_consumed) VALUES (?, ?, ?, ?)";

						PreparedStatement stmt = db.prepareStatement(query);
						stmt.setString(1, meal);
						stmt.setString(2, pass);
						stmt.setString(3, sugars);
						stmt.setString(4, saveDate);
						stmt.execute();

						System.out.println("Meal successfully logged");
					}
					catch(SQLException e){
						System.out.println("DB insert error: " + e);
					}
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
				if(!sugarsValidated)
					System.out.println(sugars + " is not a valid sugar option");
			}

		}
		else
			System.out.println("Error: 4 values must be entered");

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
		catch(SQLException e){
			System.out.println("DB Error: " + e);
		}
		
		System.exit(0);	
	}
}