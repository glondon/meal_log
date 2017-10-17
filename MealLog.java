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
		String[] menu = {
			"1. Close application",
			"2. View my whys",
			"3. View menu"
		};

		for(int i = 0; i < menu.length; i++)
			System.out.println(menu[i]);
	}

	public static void main(String[] args)
	{
		MealLog m = new MealLog();

		boolean stop = false;
		boolean parsable = true;

		m.menu();

		while(stop == false)
		{
			System.out.println("\nSelect an option\n");
			Scanner read = new Scanner(System.in);
			String value = read.next();	

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
		
		//TODO close DB connection
		System.exit(0);	
	}
}