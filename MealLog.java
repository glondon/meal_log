import java.util.Scanner;
import java.sql.*;
import java.util.Arrays;

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
			System.out.println("Entered: meal " + meal + " result: " + pass + " date: " + date);
		}
		else
			System.out.println("Error: 3 values must be entered");

		if(Arrays.asList(meals).contains(mealValue))
			System.out.println("success");
		else
			System.out.println("fail");

	}

	public static void main(String[] args)
	{
		MealLog m = new MealLog();
		Scanner read = new Scanner(System.in);

		boolean stop = false;
		boolean parsable = true;

		m.menu();

		while(stop == false)
		{
			System.out.println("\nSelect an option\n");
			String value = read.nextLine();

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
			catch(Exception ex)
			{
				parsable = false;
			}

			if(!parsable)
				System.out.println(value + " not a valid integer");
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