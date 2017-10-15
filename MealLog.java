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
		}
		catch(Exception ex)
		{
			System.out.println("DB Error: " + ex);
		}
	}

	public static void main(String[] args)
	{
		
		System.exit(0);
	}
}