import java.util.Scanner;
import java.sql.*;

public class MealLog
{

	private Connection db;
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
		//TODO turn into array
		System.out.println("1. Close application");
		System.out.println("2. View my whys");
		System.out.println("3. View menu");
	}

	public static void main(String[] args)
	{
		MealLog m = new MealLog();
		//m.getWhys();

		boolean stop = false;
		boolean parsable = true;

		while(stop == false)
		{
			System.out.println("Select an option");
			Scanner read = new Scanner(System.in);
			String value = read.next();	

			try
			{
				int entered = Integer.parseInt(value);
			}
			catch(Exception ex)
			{
				parsable = false;
			}

			if(!parsable)
				System.out.println(value + " not a valid integer");
			else
			{
				switch(entered)
				{
					case 1:
						System.out.println("Exiting Meal Log");
						System.exit(0);
						break;
					case 2:
						m.getWhys();
						break;
					case 3:
						m.menu();
						break;
					default:
						System.out.println("Not a valid entry");
						break;
				}
			}
			
			stop = true;
		}
		
		//TODO close DB connection
		System.exit(0);	
	}
}