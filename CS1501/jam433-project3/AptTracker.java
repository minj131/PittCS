import java.util.ArrayList;
import java.util.Comparator;
import java.util.Scanner;

public class AptTracker {

    @SuppressWarnings("unchecked")
    AptTracker(){

        Scanner sc = new Scanner(System.in);

        boolean found;
        int input;
        int count = 0;

        ArrayList<String> cities = new ArrayList<>();

        PQ<Apartment>[] cityRentPQ = new PQ[10];
        PQ<Apartment>[] citySqPQ = new PQ[10];

        rentComparator rentComp = new rentComparator();
        sqfootComparator sqfootComp = new sqfootComparator();


        PQ<Apartment> rentPQ = new PQ<>(10, rentComp);
        PQ<Apartment> sqfootPQ = new PQ<>(10, sqfootComp);

        String cont;    // Handles transitions in between each options (cont)inue?

        String streetAddress, city;
        int aptNumber, zip, rent, sqFootage;

        System.out.println(
                "\n ----------------------------------------- \n" +
                "|  Welcome to the Apartment Tracker App!  |" +
                "\n ----------------------------------------- \n"
        );

        while(true) {
            System.out.println("\nPlease select one of the following:");
            System.out.println("(1) Add apartment");
            System.out.println("(2) Update apartment");
            System.out.println("(3) Remove apartment");
            System.out.println("(4) Retrieve lowest price apartment");
            System.out.println("(5) Retrieve highest square footage apartment");
            System.out.println("(6) Retrieve lowest price apartment by city");
            System.out.println("(7) Retrieve highest square footage apartment by city");
            System.out.println("(0) Exit program");
            System.out.print(">");
            input = sc.nextInt();
            sc.nextLine();

            if (input > 7)
                System.out.println("\nPlease choose a valid option.");

            switch(input) {
                case 1:     // Add apartment
                    System.out.println("Adding new apartment...\n");

                    System.out.print("Street Address: ");
                    streetAddress = sc.nextLine();

                    System.out.print("Apartment Number: ");
                    aptNumber = sc.nextInt();
                    sc.nextLine();

                    System.out.print("City: ");
                    city = sc.next();
                    sc.nextLine();

                    System.out.print("ZIP Code: ");
                    zip = sc.nextInt();
                    sc.nextLine();

                    System.out.print("Rent per month: ");
                    rent = sc.nextInt();
                    sc.nextLine();

                    System.out.print("Square Footage: ");
                    sqFootage = sc.nextInt();
                    sc.nextLine();

                    Apartment addedApt = new Apartment(streetAddress, aptNumber, city, zip, rent, sqFootage);

                    while(rentPQ.contains(count)) // We find next available PQ spot
                        count++;

                    rentPQ.insert(count, addedApt);
                    sqfootPQ.insert(count, addedApt);
                    count = 0; // Reset counter for pq to 0

                    int index;

                    // Add new city into array list and update city PQ with the
                    // relevant information
                    if (!cities.contains(city)) {
                        cities.add(city);
                        index = cities.indexOf(city);

                        // Need to create a new PQ for each new city
                        PQ<Apartment> newRent = new PQ<>(10, rentComp);
                        PQ<Apartment> newSF = new PQ<>(10, sqfootComp);

                        // Then add those PQs into the original city PQs
                        newRent.insert(0, addedApt);
                        cityRentPQ[index] = newRent;

                        newSF.insert(0, addedApt);
                        citySqPQ[index] = newSF;

                    } else {    // Else city exists, add new listing
                        index = cities.indexOf(city);
                        while(cityRentPQ[index].contains(count))
                            count++;

                        cityRentPQ[index].insert(count, addedApt);
                        citySqPQ[index].insert(count, addedApt);
                        count = 0;
                    }
                    System.out.println("\nNew listing has been successfully added!");
                    break;

                case 2:     // Update apartment
                    if (rentPQ.isEmpty()) {
                        System.out.println("No listings available. Please enter a new listing.");
                        break;
                    }

                    found = false;

                    System.out.println("Updating apartment... Please enter the following:");
                    System.out.print("Street Address: ");
                    streetAddress = sc.nextLine();

                    System.out.print("Apartment Number: ");
                    aptNumber = sc.nextInt();
                    sc.nextLine();

                    System.out.print("ZIP Code: ");
                    zip = sc.nextInt();
                    sc.nextLine();

                    for (int i=0; i<rentPQ.size(); i++) {
                        Apartment curr = rentPQ.keyOf(i);
                        if (curr.streetAddress.equals(streetAddress) && curr.aptNumber == aptNumber && curr.zip == zip) {
                            found = true;
                            System.out.println("\nPlease enter the updated rent for this apartment: ");
                            System.out.print("> ");
                            rent = sc.nextInt();

                            curr.rent = rent;
                            System.out.println("Apartment has been successfully updated!");

                            System.out.print("\nWould you like to continue (y/n)? ");
                            cont = sc.next();
                            if (cont.equals("y") || cont.equals("Y"))
                                break;
                            else if (cont.equals("n") || cont.equals("N"))
                                System.exit(0);
                        }
                    }
                    if(!found){
                        System.out.println("This apartment could not be found. Please check your spelling and try again.");
                        break;
                    }
                    break;

                case 3:     // Remove apartment
                    if (rentPQ.isEmpty()) {
                        System.out.println("No listings available.");
                        break;
                    }

                    found = false;

                    System.out.println("Deleting apartment... Please enter the following:");
                    System.out.print("Street Address: ");
                    streetAddress = sc.nextLine();

                    System.out.print("Apartment Number: ");
                    aptNumber = sc.nextInt();
                    sc.nextLine();

                    System.out.print("ZIP Code: ");
                    zip = sc.nextInt();
                    sc.nextLine();

                    // Same process as update, but we're just removing the listing
                    for (int i=0; i<rentPQ.size(); i++) {
                        Apartment curr = rentPQ.keyOf(i);

                        if (curr.streetAddress.equals(streetAddress) && curr.aptNumber == aptNumber && curr.zip == zip) {
                            found = true;
                            rentPQ.delete(i);
                            sqfootPQ.delete(i);
                            System.out.println("Apartment has been successfully deleted!");

                            System.out.print("\nWould you like to continue (y/n)? ");
                            cont = sc.next();
                            if (cont.equals("y") || cont.equals("Y"))
                                break;
                            else if (cont.equals("n") || cont.equals("N"))
                                System.exit(0);
                        }
                    }
                    if(!found){
                        System.out.println("This apartment could not be found. Please check your spelling and try again.");
                        break;
                    }
                    break;

                case 4:     // Retrieve lowest priced apartment
                    if (rentPQ.isEmpty()) {
                        System.out.println("No listings available. Please enter a new listing.");
                        break;
                    }

                    System.out.println("\nRetrieved apartment by lowest rent:");
                    System.out.println(rentPQ.minKey());

                    System.out.print("\nWould you like to continue (y/n)? ");
                    cont = sc.next();
                    if (cont.equals("y") || cont.equals("Y"))
                        break;
                    else if (cont.equals("n") || cont.equals("N"))
                        System.exit(0);

                case 5:     // Retrieve highest sq footage apartment
                    if (sqfootPQ.isEmpty()) {
                        System.out.println("No listings available. Please enter a new listing.");
                        break;
                    }

                    // The comparator here just takes the opposite min (which is max)
                    System.out.println("\nRetrieved apartment by highest square footage:");
                    System.out.println(sqfootPQ.minKey());


                    System.out.print("\nWould you like to continue (y/n)? ");
                    cont = sc.next();
                    if (cont.equals("y") || cont.equals("Y"))
                        break;
                    else if (cont.equals("n") || cont.equals("N"))
                        System.exit(0);

                case 6:     // Retrieve lowest priced apartment by city
                    if (rentPQ.isEmpty()) {
                        System.out.println("No listings available. Please enter a new listing.");
                        break;
                    }

                    System.out.println("\nPlease enter a city: ");
                    System.out.print(">");
                    city = sc.next();

                    // Same process as options 4 and 5, but instead we're checking by city
                    if (cities.contains(city)) {
                        index = cities.indexOf(city);

                        System.out.println("\nRetrieved lowest priced apartment in " + city + ": ");
                        System.out.println(cityRentPQ[index].minKey());
                    } else
                        System.out.println("\nThere are no current listings in " + city + ".");


                    System.out.print("\nWould you like to continue (y/n)? ");
                    cont = sc.next();
                    if (cont.equals("y") || cont.equals("Y"))
                        break;
                    else if (cont.equals("n") || cont.equals("N"))
                        System.exit(0);

                case 7:     // Retrieve highest sq footage apartment by city
                    if (sqfootPQ.isEmpty()) {
                        System.out.println("No listings available. Please enter a new listing.");
                        break;
                    }

                    System.out.println("\nPlease enter a city: ");
                    System.out.print(">");
                    city = sc.next();

                    if (cities.contains(city)) {
                        index = cities.indexOf(city);

                        System.out.println("\nRetrieved lowest priced apartment in " + city + ": ");
                        System.out.println(citySqPQ[index].minKey());
                    } else
                        System.out.println("\nThere are no current listings in " + city + ".");


                    System.out.print("\nWould you like to continue (y/n)? ");
                    cont = sc.next();
                    if (cont.equals("y") || cont.equals("Y"))
                        break;
                    else if (cont.equals("n") || cont.equals("N"))
                        System.exit(0);

                case 0:     // Exit program
                    System.out.println("\nGoodbye!");
                    System.exit(0);
            }
        }

    }

    public static void main(String[] args) {new AptTracker();}

    class Apartment {
        String streetAddress;
        int aptNumber;
        String city;
        int zip;
        int rent;
        int sqFootage;

        // Constructor for Apartment
        public Apartment(String streetAddress, int aptNumber, String city, int zip, int rent, int sqFootage){
            this.streetAddress = streetAddress;
            this.aptNumber = aptNumber;
            this.city = city;
            this.zip = zip;
            this.rent = rent;
            this.sqFootage = sqFootage;
        }

        public String toString(){
            return "Street Address: " + streetAddress +
                    "\nApartment Number: " + aptNumber +
                    "\nCity: " + city +
                    "\nZip Code: " + zip +
                    "\nRent per Month: " + rent +
                    "\nSquare Footage: " + sqFootage;
        }
    }

    class rentComparator implements Comparator<Apartment> {
        public int compare(Apartment one, Apartment two) {
            return one.rent - two.rent;
        }
    }

    class sqfootComparator implements Comparator<Apartment> {
        public int compare(Apartment one, Apartment two) {
            return -(one.sqFootage - two.sqFootage);
        }
    }
}
