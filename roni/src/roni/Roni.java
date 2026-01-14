// Add header comment (for recruiters viewing code)
/* 
 * AIRLINE RESERVATION SYSTEM 
 * Features: User auth, seat booking, admin controls 
 * Security: OTP via phone, SHA-256 hashing
 * Data: Persistent storage via serialization
 */
package roni;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.lang.foreign.Linker.Option;
import java.lang.foreign.ValueLayout;
import java.security.MessageDigest;
import java.util.*;
class Users implements Serializable
{
		private static final long serialVersionUID=1L;
		String[] details;
		String password;
		String travellocation;
		String phone;
		Users(String[] details,String password)
		{
			this.details=details;
			this.password=password;
			this.travellocation="";
			this.phone=details[2];
		}
		public void displayprofile() {
		    String[] label = {"name", "Date of birth", "phone-no", "gender", "email", "verfiyid", "location"};
		    System.out.println("-----user profile-----");
		    for (int i = 0; i < details.length; i++) {
		        System.out.println(label[i] + ":" + details[i]);
		    }
		    System.out.println("last travels is " + ((travellocation == null || travellocation.isEmpty()) ? "N/A" : travellocation));
		}
	
	}
	public class Roni 
	{
		
		private static final String DATA_FILE="d:\\anush\\";
		private static final String USER_DATA_FILE=DATA_FILE+"airline_userdata.txt";
		private static final String SEAT_DATA_FILE=DATA_FILE+"airline_seatdata.txt";
		private static HashMap<String, Users> user=new HashMap<>();
		private static HashMap<String,HashMap<Integer, String>>globalseatbylocation=new HashMap<>();
		private static final int seats=10;
		private static final String ADMIN_TRIGGER="---b---";
		private static final String ADMIN_USERNAME="r";
		private static final String ADMIN_PASSWORD="s";
		public enum otpstatus
		{
			sucess,
			fail,
			timeout,
			cancel
		}
		public static void main(String[]args)
		{
			
			new File(DATA_FILE).mkdirs();
			LoadUserFromFile();
			loadglobalseats();
			Scanner sa=new Scanner(System.in);
			int sh;
			do 
			{	
				System.out.println("----airline reservation system----");
				System.out.println("1.login");
				System.out.println("2.signin");
				System.out.println("3.delete account");
				System.out.println("4.change password");
				System.out.println("5.exit");
				try 
				{
					sh=Integer.parseInt(sa.nextLine().trim());
					switch(sh)
					{
					case 1:login(sa,null);break;
					case 2:signin(sa);
					saveUserToFile();break;
					case 3:deleteaccount(sa);
					saveUserToFile();break;
					case 4:changepassword(sa);
					saveUserToFile();break;
					case 5:
						saveUserToFile();
						saveglobalseats();
					System.out.println("Thanks for using our airline reservation app");
					sa.close();
					return;
					default:System.out.println("invalid number please enter correct");break;
					}
				} catch (Exception e) 
				{
					System.out.println(e.getMessage());
				}
			}while(true);
		}
		public static void login(Scanner s,String atlocation)
		{
			String username,adminusername;
			String password,adminpassword;
			while(true)
			{
				System.out.print("enter the username :");
				username=s.nextLine().trim();
				if(username.equals(ADMIN_TRIGGER))
				{
				
					System.out.print("admin username : ");
					adminusername=s.nextLine().trim();
					if(adminusername.isEmpty())
					{
						System.out.println("admin username is missing please enter");
						continue;
					}
					System.out.print("admin password : ");
					adminpassword=s.nextLine().trim();
					if(adminpassword.isEmpty())
					{
						System.out.println("admin password is missing please enter");
						continue;				
					}
					if(adminusername.equals(ADMIN_USERNAME)&&adminpassword.equals(ADMIN_PASSWORD))
					{
						System.out.println("admin logged in succesfully");
						admindashboard(s,username,null,0);
					}
					else 
					{
						System.out.println("invaild admin credentials");
					}return;
				}
				if(username.isEmpty())
				{
					System.out.println("username is missing please enter");
					continue;
				}
				if(!user.containsKey(username))
	        	{
	        		System.out.println("username is not found please signin first");
	        		return;
	        	}
	        	break;       
			}
			Users currentUsers=user.get(username);
			int attempt=3;
			while(attempt>0)
			{
				System.out.print("enter the password :");
	        	password=s.nextLine().trim();
	        	if(password.isEmpty())
	        	{
	        		System.out.println("password is missing please enter");
	        		continue;
	        	}
	        	if(currentUsers.password.equals(password))
	        	{
	        		
	        		System.out.println("you have logged in succesfully");
	        		currentUsers.displayprofile();
	        		location(s,username);
	        		return;
	       		}
	        	else
	        	{
	        		attempt--;
	        		if(attempt>0)
	        		{
	        		System.out.println("incorrect password "+attempt+" attempts left");
	        		}
	        		else
	            	{
	    				System.out.print("too many failed attempts returns to main ");
	    			}
				}
			}
		}
		public static void signin(Scanner s)
		{
			String usernamesignin;
			String passwordsignin;
			String[]  details= {"Name", "Date of Birth (yyyy-mm-dd)", "Phone Number", "Gender (M/F/O)", "Email", "Verify ID", "Location"};
	        String[] fields = new String[details.length];
	        for (int i = 0; i < details.length; i++) 
	        {
	            while (true) 
	            {
	                System.out.println("Enter your " + details[i] + ":");
	                String input = s.nextLine().trim();
	                if (input.isEmpty()) 
	                {
	                    System.out.println(details[i] + " is missing. Please enter.");
	                    continue;
	                }
	                if (!validateField(i, input)) 
	                {
	                    System.out.println("Invalid " + details[i] + ". Please enter a valid value.");
	                    continue;
	                }
	                fields[i] = input;
	                break;
	            }
	        }
	        System.out.println("your details is filed successfully");
	        while(true)
	        {
	        	System.out.println("create the username");
	        	usernamesignin=s.nextLine().trim();
	        	if(usernamesignin.isEmpty())
	        	{
	        		System.out.println("username is missing please enter");
	        		continue;
	        	}
	        	if(user.containsKey(usernamesignin))
	        	{
	        		System.out.println("username is already existed");
	        		continue;
	        	}
	        	break;
	        }
	        while(true)
	        {
	        	System.out.println("create the password");
	        	passwordsignin=s.nextLine().trim();
	        	if(passwordsignin.isEmpty())
	        	{
	        		System.out.println("password is missing please enter");
	         		continue;
	        	}
	        	otpstatus status=OTP(s, fields[2]);
	        	if(status!=otpstatus.sucess)
	        	{
	        		System.out.println("signin cancelled due to OTP verification failed");
	        		return;
	        	}
	        	String hashedpass=hashpassword(passwordsignin);
	        	user.put(usernamesignin,new Users(fields,hashedpass));
	    		System.out.println("your account is created");
	    		break;
	        }
		}
		public static void deleteaccount(Scanner s)
		{
			String userdelete,conform,password;
				System.out.println("----delete----");
	        	System.out.println("enter your username");
	        	userdelete=s.nextLine().trim();
	        	if(!user.containsKey(userdelete))
	        	{
	        		System.out.println("username is not found");
	        		return;
	        	}
	        	
	        	System.out.println("enter your password");
	        	password=s.nextLine().trim();
	    		Users currentUsers=user.get(userdelete);
	        	if(!currentUsers.password.equals(password))
	        	{
	        		System.out.println("incorrect password");
	        		return;
	        	}
	        	
	        	System.out.println("are you sure to delete your account?(yes/no)");
	        	conform=s.nextLine().trim();
	        	if(conform.equalsIgnoreCase("yes"))
	        	{
	        		user.remove(userdelete);
	        		System.out.println("your account deleted");
	        	}
	        	else 
	        	{
	            		System.out.println("account deletion canceled");
	            		return;
	        	}
		}
		public static void changepassword(Scanner s)
		{
			String userchange,conform,oldpassword,newpassword;
			System.out.println("----change password----");
	    	System.out.println("enter your the username");
	    	userchange=s.nextLine().trim();
	    	if(!user.containsKey(userchange))
	    	{
	    		System.out.println("username is not found");
	    		return;
	    	}
	    	
	    	System.out.println("enter your old password");
	    	oldpassword=s.nextLine().trim();
			Users currentUsers=user.get(userchange);
	    	if(!currentUsers.password.equals(oldpassword))
	    	{
	    		System.out.println("incorrect password");
	    		return;
	    	}
	    	
	    	System.out.println("enter your new password");
	    	newpassword=s.nextLine().trim();
			if(newpassword.isEmpty())
			{
				System.out.println("password is missing please enter");
	    		return;
			}
	    	System.out.println("are you sure to change your password?(yes/no)");
	    	conform=s.nextLine().trim();
	    	if(conform.equalsIgnoreCase("yes"))
	    	{
	    		String hashedpassword=hashpassword(newpassword);
	    		currentUsers.password=hashedpassword;
	    		System.out.println("your password is changed");
	    	}
	    	else 
	    	{
	        		System.out.println("password changing is canceled");
	        		return;
	    	}
		}
		public static void saveUserToFile()
		{
			try (ObjectOutputStream o=new ObjectOutputStream(new FileOutputStream(USER_DATA_FILE)))
			{
				o.writeObject(user);
				System.out.println("user data is updated");
			} catch (Exception e) {
				System.out.println("error saving user data ");
				e.printStackTrace();
			}
		}
		@SuppressWarnings("unchecked")
		public static void LoadUserFromFile()
		{
			try (ObjectInputStream os=new ObjectInputStream(new FileInputStream(USER_DATA_FILE)))
			{
				user=(HashMap<String, Users>)os.readObject();
				System.out.println("---previous data is uploaded!---");						
			} catch (Exception e) {
				System.out.println("no previous data not found!start new ");
				e.printStackTrace();
				user=new HashMap<>();
			}
		}	
		@SuppressWarnings("unchecked")
		public static void loadglobalseats()
		{
			try (ObjectInputStream oi = new ObjectInputStream(new FileInputStream(SEAT_DATA_FILE)))
			{
			globalseatbylocation=(HashMap<String, HashMap<Integer, String>>)oi.readObject();
			System.out.println("your seat data is uploaded sucessfully");
			}
			catch(Exception e)
			{
				System.out.println("your pervious seat data not found! starting fresh");
				e.printStackTrace();
				globalseatbylocation=new HashMap<>();
			}
		}
		public static void saveglobalseats()
		{
			try (ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(SEAT_DATA_FILE))) 
			{
				os.writeObject(globalseatbylocation);
				System.out.println("seats data is saved");			
			} 
			catch (Exception e) 
			{
				System.out.println("failed to save seat data ");
	//			e.printStackTrace();
			}
			
		}
		
		public static boolean validateField(int index,String input)
		{
			switch(index)
			{
			case 0:return input.matches("[a-zA-Z]{1,100}");
			case 1:return input.matches("\\d{4}-\\d{2}-\\d{2}");
			case 2:return input.matches("\\d{10}");
			case 3:return input.equalsIgnoreCase("M")||input.equalsIgnoreCase("F")||input.equalsIgnoreCase("O");
			case 4:return input.matches("^[\\w.-]+@[\\w.-]+\\.\\w{2,}$");
			case 5:return input.length()>=5;
			case 6:return input.length()>=2;
			default:return false;
			}
		}
		public static void avaliableseats(String atlocation)
		{
			HashMap<Integer,String>seatmap=globalseatbylocation.get(atlocation);
			if(seatmap==null)
			{
				System.out.println("no seats are available at this location");
				return;
			}
			for(int i=0;i<seats;i++)
			{
				if(seatmap.containsKey(i+1))
				{
					System.out.println("seat "+(i+1)+" is already booked");
				}
				else
				{
					System.out.println("seat "+(i+1)+" is available");
				}
			}
		}
		public static void booking(Scanner s,String username,String atlocation,int totalcharges)
		{
			HashMap<Integer, String> seatmap=globalseatbylocation.get(atlocation);
			if(seatmap==null)
			{
				System.out.println("no seats are available at this location");
				return;
			}
			try 
			{
				System.out.println("please enter the seat no you want to reserv");
				int seatno=Integer.parseInt(s.nextLine());
				if(seatno<1||seatno>seats)
				{
					System.out.println("invalid seatno please enter correct");
				}
				else if(seatmap.containsKey(seatno))
				{
					System.out.println("seatno "+seatno+" IS ALREADY BOOKED");			
				}
				else 
				{
					otpstatus result=payment(s,totalcharges,username);
					if(result==otpstatus.sucess)
					{
						seatmap.put(seatno,username);
						System.out.println("payment sucessfully");
						System.out.println("your seatno "+seatno+" is booked for "+atlocation);
					}
					else if (result==otpstatus.cancel) 
					{	
						System.out.println("payment is cancelled by user");
					}
					else 
					{					
						System.out.println("payment failed or cancelled. seat not booked");
					}
				}
			} 
			catch (NumberFormatException e) 
			{
				System.out.println("invalid value please try again");
			}
		}
		public static void booking(Scanner s,String username,String atlocation)
		{
			booking(s, username, atlocation,0);
		}
		public static void canceling(Scanner s,String username,String atlocation)
		{
			HashMap<Integer, String> seatmap=globalseatbylocation.get(atlocation);
			if(seatmap==null)
			{
				System.out.println("no seats are available at this location");
				return;
			}
			try {
				System.out.println("please enter the seat no you want to cancel");
				int seatno=Integer.parseInt(s.nextLine());
				if(seatno<1||seatno>seats)
				{
					System.out.println("invalid seatno please enter correct");
				}
				else if(!seatmap.containsKey(seatno))
				{
					System.out.println("seatno "+seatno+" is not booked its available");			
				}
				else if (!seatmap.get(seatno).equals(username))
				{
					System.out.println("You can't cancel the seatno "+seatno+" because its not booked by you");
				}
				else 
				{
					System.out.println("are you sure to cancel your seat registeration ?(yes/no)");
					String ca=s.nextLine();
					if(ca.equalsIgnoreCase("yes"))
					{
						seatmap.remove(seatno);
						System.out.println("your seatno "+seatno+" has been canceled for"+atlocation);
					}
					else 
					{
						System.out.println("your cancelation is failed please try again");					
					}
				}
				
				} catch (NumberFormatException e) {
					System.out.println("invalid value please try again");
				}
		}
		public static void checking(Scanner s,String username,String atlocation)
		{
			HashMap<Integer, String>seatmap=globalseatbylocation.get(atlocation);
			if(seatmap==null)
			{
				System.out.println("no seats are available at this location");
				return;
			}
			try 
			{
				System.out.println("please enter the seat no you want to check");
				int seatno=Integer.parseInt(s.nextLine());
				if(seatno<1||seatno>seats)
				{
					System.out.println("invalid seatno please enter correct");
				}
				else if(!seatmap.containsKey(seatno))
				{
					System.out.println("seatno "+seatno+" is available not booked by at all");			
				}
				else if (seatmap.get(seatno).equals(username)) 
				{
					System.out.println("seatno "+seatno+" is reserved by you");							
				}
				else
				{
					System.out.println("seatno "+seatno+" is already booked by other user");
				}		
			} 
			catch (NumberFormatException e) 
			{
				System.out.println("invalid value please try again");		
			}
		}
		public static otpstatus payment(Scanner s,int totalcharges,String username)
		{
			System.out.println("---payment gateway---");
			System.out.println("total amount : "+totalcharges);
			System.out.println("choose the payment method");
			System.out.println("1.UPI");
			System.out.println("2.Credit/debit card");
			System.out.println("3.Net banking");		
			System.out.println("4.cancel payment");
			int pay;
			try 
			{
				 pay=Integer.parseInt(s.nextLine().trim());			
			} 
			catch (Exception e) 
			{
				System.out.println("invalid input payment failed");
				return otpstatus.fail;
			}
			Users currentUsers=user.get(username);
			if(currentUsers==null)
			{
				System.out.println("user not found");
				return otpstatus.fail;
			}
			String phone=currentUsers.phone!=null?currentUsers.phone:currentUsers.details.length>2?currentUsers.details[2]:null;
			if(phone==null||phone.isEmpty())
			{
				System.out.println("no phone number registered for otp");
				return otpstatus.fail;
			}
			if(pay==4)
			{
				System.out.println("payment cancelled");
				return otpstatus.cancel;
			}
			if(pay<1 || pay>4)
			{
				System.out.println("invaild input.payment failed");
				return otpstatus.fail;
			}
			System.out.println("payment processing....");
			try 
			{
			Thread.sleep(1000);	
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
			}
			if(pay==2)
			{
				/*System.out.println("otp has been send to registered moblie number ");
				int otp=100000+new Random().nextInt(900000);
				System.out.println("simlated otp : "+otp);
				System.out.print("please enter the opt : ");
				String input=s.nextLine().trim();
				if(!input.equals(String.valueOf(otp)))
				{
					System.out.println("incorrect otp payment failed");
					return otpstatus.fail;
				}*/
				OTP(s, phone);
			}
			return otpstatus.sucess;
		}
		public static void location(Scanner s,String username)
		{
			System.out.println("\n---welcome to the airline reservation website--- ");
			System.out.println("enter the location u want to travel");
			String[] location= {"tirupati","hyderabad","chennai","delhi","mumbai"};
			Integer[] distance= {500,400,350,1300,1400};
			int charges=8;
			try
			{
				for(int i=0;i<location.length;i++)
				{
					System.out.println((i+1)+"."+location[i]+"(distance = "+distance[i]+")");
				}
				int choice=Integer.parseInt(s.nextLine().trim());
				if(choice<1||choice>location.length)
				{
					System.out.println("invaild location number");
					return;
				}
				String atlocation=location[choice-1];
				int atdistance=distance[choice-1];
				int totalcharges=charges*atdistance;
				
				System.out.println("your selected location is "+atlocation);
				System.out.println("distance to travel is "+atdistance+"km");
				System.out.println("total charges : "+totalcharges);
				
				Users currentUsers=user.get(username);
				currentUsers.travellocation=atlocation;
				
				globalseatbylocation.putIfAbsent(atlocation,new HashMap<>());
				System.out.println("redirecting to the dashboard");
				userdashboard(s, username,atlocation,totalcharges);
			}
			catch(Exception e)
			{
				System.out.println("invaild selection please enter correct"+e.getMessage());			
			}
		}
		public static void userdashboard(Scanner s,String username, String atlocation,int totalcharges)
		{
			int air;
			do 
			{	
				System.out.println("\n---welcome to the dashboard---");
				System.out.println("1.view avaliable seats");
				System.out.println("2.book a seat");
				System.out.println("3.cancel the seat");
				System.out.println("4.check reservation status");
				System.out.println("5.exit");
				try 
				{
					air=Integer.parseInt(s.nextLine().trim());
					switch(air)
					{
						case 1:avaliableseats(atlocation);break;
						case 2:booking(s,username,atlocation,totalcharges);
						saveglobalseats();break;
						case 3:canceling(s,username,atlocation);
						saveglobalseats();break;
						case 4:checking(s,username,atlocation);break;
						case 5:
							System.out.println("Thanks for using our website");
							return;
						default:System.out.println("invalid number please enter correct");break;
					}
				} catch (Exception e) 
				{
					System.out.println("invalid selection please enter correct");
				}
			}while(true);
		}
		public static void admindashboard(Scanner s,String username,String atlocation,int totalcharges)
		{
			int air;
			if(atlocation==null||atlocation.isEmpty())
			{
				System.out.println("admin: select the location to manage booking");
				String[] location= {"tirupati","hyderabad","chennai","delhi","mumbai"};
				for(int i=0;i<location.length;i++)
				{
					System.out.println((i+1)+"."+location[i]);
				}
				try 
				{
					int choice=Integer.parseInt(s.nextLine().trim());
					if(choice>=1&&choice<=location.length)
					{
						atlocation=location[choice-1];
					}
					else 
					{
						System.out.println("please select the correct value");
						return;
					}
				}
				catch (Exception e) 
				{
					System.out.println("invaild number");
					return;
				}
			}
			globalseatbylocation.putIfAbsent(atlocation,new HashMap<>());
			do 
			{	
				System.out.println("\n---welcome to the airline reservation website--- ");	
				System.out.println("1.view avaliable seats");
				System.out.println("2.book a seat");
				System.out.println("3.cancel the seat");
				System.out.println("4.check reservation status");
				System.out.println("5.view all users profiles");
				System.out.println("6.view all booking with users");
				System.out.println("7.delete all users ");
				System.out.println("8.backup all users and bookings");
				System.out.println("9.reset all booked seats");
				System.out.println("10.identify user by username ");
				System.out.println("11.reset the users password ");
				System.out.println("12.export to csv");
				System.out.println("13.exit");
				try 
				{
					air=Integer.parseInt(s.nextLine().trim());
					switch(air)
					{
					case 1:avaliableseats(atlocation);break;
					case 2:booking(s,username,atlocation,0);
					saveglobalseats();break;
					case 3:canceling(s,username,atlocation);
					saveglobalseats();break;
					case 4:checking(s,username,atlocation);break;
					case 5:usersprofile();break;
					case 6:allbookingusers(atlocation);break;
					case 7:deleteallusers(s,username);break;
					case 8:backup(s,username);break;
					case 9:resetbookings(s,username);
					saveglobalseats();break;
					case 10:byusername(s);break;
					case 11:resetpassword(s);
					saveglobalseats();break;
					case 12:exportusersTOCSV();break;
					case 13:
					System.out.println("exiting the admin panel");
					return;
					default:System.out.println("invalid number please enter correct");break;
					}
				} catch (Exception e) 
				{
					System.out.println(e.getMessage());
				}
			}while(true);
		}
		public static void usersprofile()
		{
			System.out.println("---users profiles---");
			if(user.isEmpty())
			{
				System.out.println("no user profiles are available");
			}
			int count=1;
			for(Map.Entry<String,Users>entry:user.entrySet())
			{
				String uname=entry.getKey();
				Users pass=entry.getValue();
				System.out.println("\nuser #"+count++ +"\n----------------------");
				System.out.println("username : "+uname);
				System.out.println("password : "+pass.password+"\n");
				pass.displayprofile();
			}
		}
		public static void allbookingusers(String atlocation)
		{
			HashMap<Integer, String>seatno=globalseatbylocation.get(atlocation);
			if(seatno==null) 
			{
				System.out.println("no bookings found for this location");
				return;
			}
			System.out.println("----all bookings----");
			for(int i=1;i<=seats;i++)
			{
				if(seatno.containsKey(i))
				{
					System.out.println("seat " + i +" is booked by the user "+seatno.get(i)+" for "+atlocation);
				}
				else
				{
					System.out.println("seat " + i +" is not booked by anyone");
				}
			}
		}
		public static void deleteallusers(Scanner s, String username) 
		{
			System.out.println("do you want to backup the userdata before deleting ? yes/no");
			String backup=s.nextLine().trim();
			if(backup.equalsIgnoreCase("yes"))
			{
				exportusersTOCSV();
				System.out.println("your user data is backed up to cvs file");
			}
			
		    System.out.println("Are you sure you want to delete all users and bookings? (yes/no)");
		    String confirmation = s.nextLine().trim();
	
		    if (!confirmation.equalsIgnoreCase("yes")) 
		    {
		    	System.out.println("Deletion cancelled. No users or bookings were deleted.");
		    	return;
		    }
		    user.clear();
	        globalseatbylocation.clear();
	        File seatFile=new File(SEAT_DATA_FILE);
	        File userFile=new File(USER_DATA_FILE);	        
	        if(seatFile.exists()&&seatFile.delete())
	        {	
	        	System.out.println("seat data file deleted sucessfully");
	        }	
	       	else 
	        {
	           	System.out.println("failed to delete seat data file"+seatFile.getAbsolutePath());	        
	        }
	        if(userFile.exists()&&userFile.delete())
	        {	
	       		System.out.println("user data file deleted sucessfully");
	        }
	       	else 
	       	{
	           	System.out.println("failed to delete user data file"+userFile.getAbsolutePath());	        
	       	}
	        System.out.println("all users and booking are cleared");
		}
		public static void backup(Scanner s,String username)
		{
			
		}
		public static void byusername(Scanner s)
		{
	        System.out.println("enter the username you want to search");
			String search=s.nextLine().trim();
			
			if (user.containsKey(search))
			{
				System.out.println("---user found---");
				user.get(search).displayprofile();
			}
			else
			{
				System.out.println("---user not found---");						
			}
		}
		public static void resetpassword(Scanner s)
		{
			System.out.println("enter the user name u want to change the passsword");
			String username=s.nextLine().trim();
			if(user.containsKey(username))
			{
				System.out.println("Are you sure you want to reset the users passwords? (yes/no)");
			    String confirmation = s.nextLine().trim();
			    if(confirmation.equalsIgnoreCase("yes"))
			    {
			    	System.out.println("enter the new password");
			    	String newpass=s.nextLine().trim();
			    	String hashedpass=hashpassword(newpass);
			    	user.get(username).password=hashedpass;
			    	saveUserToFile();
			    	System.out.println("password is reset sucessfully");
			    }
			}
			else if (!user.containsKey(username))
			{
				System.out.println("incorrect username please enter correct username");
			}
			else
			{
				System.out.println("---user not found---");						
			}
		}
		public static void resetbookings(Scanner s, String username)
		{
			System.out.println("enter the location to reset");
		    String loc = s.nextLine().trim();
		    
		    Set<String> locat = Set.of("tirupati", "hyderabad", "chennai", "delhi", "mumbai");
		    if (!locat.contains(loc)) 
		    {
		        System.out.println("invalid location");
		        return;
		    }
		    if (!globalseatbylocation.containsKey(loc)) 
		    {
		    	System.out.println("no bookings found for this location");
		    } 
		    System.out.println("are u sure you want to reset all booking at "+loc+"?(yes/no)");
		    String confirm=s.nextLine().trim();
		    if(!confirm.equalsIgnoreCase("yes"))
		    {
		    	System.out.println("reset cancelled");
		    }
		        globalseatbylocation.get(loc).clear();
		        System.out.println("all seat bookings at " + loc + " are reset");
		}
	
		public static void exportusersTOCSV()
		{
			String filename="d:\\anush\\user_export_" + System.currentTimeMillis() +".csv";
			try(PrintWriter p=new PrintWriter(filename)) 
			{
				p.println("username,password,name,dob,phone,gender,email,verfyid,location,travellocation");
				for(Map.Entry<String,Users>entry:user.entrySet())
				{
					String[] d=entry.getValue().details;
					p.println("\""+entry.getKey()+"\",\""+entry.getValue().password+"\",\""+String.join("\",\"",d)+"\",\""+entry.getValue().travellocation+"\"");
				}
				System.out.println("exported successfully");
			}
			catch (Exception e) 
			{
				System.out.println("error in exporting data"+e.getMessage()); 
			}
		}
		public static otpstatus OTP(Scanner s,String phone)
		{
			int maxresendattempt=3;
			int	maxwrongattempt=3;
			int resendattempt=0;
			int	wrongattempt=0;
			long timeout=60*1000;
	    	while(true)
	    	{
	    		System.out.println("OTP has been send to your phone number : ******"+phone.substring(phone.length()-4));
	    		int otp=new Random().nextInt(100000,900000);
	    		try 
	    		{
	    			Thread.sleep(1000);
	    		}
	    		catch (Exception e) 
	    		{
	    			System.out.println("interrputed while sending OTP");
	    		}
	    		System.out.print("simulated OTP : "+otp);
	    		System.out.print("\nenter the OTP recevied on your phone or resend to get a new otp:");
	    		Long starttime=System.currentTimeMillis();
	    		String input=s.nextLine().trim();
	    		if(System.currentTimeMillis()-starttime>timeout)
	    		{
	    			System.out.println("otp timed out.please try agian");
	    			return otpstatus.timeout;
	    		}
	    		if(resendattempt>=maxresendattempt)
	    		{
	    			System.out.println("maximum otp resends limit is reached");
	    			return otpstatus.fail;
	    		}
	    		if(input.equalsIgnoreCase("resend"))
	    		{
	    			resendattempt++;
	    			continue;
	    		} 
	        	if(input.equals(String.valueOf(otp)))
	        	{
	        		System.out.println("OTP verified succesfully");
	        		return otpstatus.sucess;
	    		}
	        	else
	        	{
	        		wrongattempt++;
	        		System.out.println("incorrect otp please try again "+(maxwrongattempt-wrongattempt)+" attempts left");
	        		if(wrongattempt>=maxwrongattempt)
	        		{
	        			System.out.println("too many failed attempts otp.verification failed");
	        	    	return otpstatus.fail;			
	        		}
	        	}
	    	}	
	    	
		}
		public static String hashpassword(String password)
		{
			try 
			{
				MessageDigest md=MessageDigest.getInstance("SHA-256");
				byte[] hashbyte=md.digest(password.getBytes());
				StringBuilder sd=new StringBuilder();
						for(byte b:hashbyte)
						{
							sd.append(String.format("%02x",b));
						}
						return sd.toString();
			}
			catch(Exception e)
			{
				e.printStackTrace();
				return null;
			}
		}
	}