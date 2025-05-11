import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

class Flight {
    String flightNumber;
    String source;
    String destination;
    int totalSeats;
    int availableSeats;
    double basePrice;
    String departureTime;
    Queue<String> waitlist = new LinkedList<>();

    public Flight(String flightNumber, String source, String destination, int totalSeats, double basePrice, String departureTime) {
        this.flightNumber = flightNumber;
        this.source = source;
        this.destination = destination;
        this.totalSeats = totalSeats;
        this.availableSeats = totalSeats;
        this.basePrice = basePrice;
        this.departureTime = departureTime;
    }

    public int bookSeat(String passengerName, int numTickets) {
        int booked = 0;
        for (int i = 0; i < numTickets; i++) {
            if (availableSeats > 0) {
                availableSeats--;
                booked++;
            } else {
                waitlist.offer(passengerName + " (Ticket " + (i + 1) + ")");
            }
        }
        return booked;
    }

    public void cancelSeat() {
        availableSeats++;
        if (!waitlist.isEmpty()) {
            String nextPassenger = waitlist.poll();
            availableSeats--; 
            System.out.println("Booking confirmed for waitlisted passenger: " + nextPassenger);
        }
    }

    public double getPrice(int age) {
        if (age <= 2) return 0.0;
        else if (age <= 12) return basePrice * 0.5;
        else return basePrice;
    }

    public String toString() {
        return "Flight from " + source + " to " + destination + " | Available Seats: " + availableSeats;
    }
}

class FlightNode {
    Flight flight;
    FlightNode next;

    public FlightNode(Flight flight) {
        this.flight = flight;
        this.next = null;
    }
}

class BookingSystem {
    FlightNode head;
    // Map of username to list of booking records
    private Map<String, List<BookingRecord>> userBookings = new HashMap<>();

    static class BookingRecord {
        Flight flight;
        String passengerName;

        BookingRecord(Flight flight, String passengerName) {
            this.flight = flight;
            this.passengerName = passengerName;
        }
    }

    public void addFlight(Flight flight) {
        FlightNode newNode = new FlightNode(flight);
        if (head == null) {
            head = newNode;
        } else {
            FlightNode current = head;
            while (current.next != null) {
                current = current.next;
            }
            current.next = newNode;
        }
    }

    public void displayFlights() {
        if (head == null) {
            System.out.println("No flights available.");
            return;
        }
        FlightNode current = head;
        int index = 1;
        while (current != null) {
            System.out.println(index + ". " + current.flight.source + " to " + current.flight.destination);
            current = current.next;
            index++;
        }
    }

    public Flight getFlight(int index) {
        FlightNode current = head;
        int i = 0;
        while (current != null && i < index) {
            current = current.next;
            i++;
        }
        return current != null ? current.flight : null;
    }

    public void displayFlightsByType(boolean isInternational, String username) {
        if (head == null) {
            System.out.println("No flights available.");
            return;
        }

        Scanner scanner = new Scanner(System.in);

        List<String> domesticCities = Arrays.asList("Delhi", "Mumbai", "Chennai", "Kolkata", "Bangalore", "Hyderabad", "Jaipur", "Ahmedabad", "Goa");
        List<String> internationalCities = Arrays.asList("Singapore", "London", "Dubai", "Bangkok", "New York", "Paris", "Tokyo", "Toronto");

        Set<String> validDestinations = new HashSet<>();
        FlightNode current = head;
        while (current != null) {
            String dest = current.flight.destination;
            String src = current.flight.source;
            if ((isInternational && internationalCities.contains(dest)) ||
                (!isInternational && domesticCities.contains(dest))) {
                validDestinations.add(dest);
            }
            current = current.next;
        }

        if (validDestinations.isEmpty()) {
            System.out.println("No valid destinations found for this flight type.");
            return;
        }

        List<String> availableCities = new ArrayList<>(validDestinations);
        System.out.println("Available destination cities:");
        for (int i = 0; i < availableCities.size(); i++) {
            System.out.println((i + 1) + ". " + availableCities.get(i));
        }

        System.out.print("Select destination city (enter the number): ");
        int destinationChoice = scanner.nextInt();
        scanner.nextLine(); 
        String destinationCity = availableCities.get(destinationChoice - 1);

        Set<String> validSources = new HashSet<>();
        current = head;
        while (current != null) {
            if (current.flight.destination.equalsIgnoreCase(destinationCity)) {
                validSources.add(current.flight.source);
            }
            current = current.next;
        }

        if (validSources.isEmpty()) {
            System.out.println("No source cities found for destination: " + destinationCity);
            return;
        }

        List<String> sourceOptions = new ArrayList<>(validSources);
        System.out.println("Available source cities for " + destinationCity + ":");
        for (int i = 0; i < sourceOptions.size(); i++) {
            System.out.println((i + 1) + ". " + sourceOptions.get(i));
        }
        System.out.print("Select source city (enter the number): ");
        int sourceChoice = scanner.nextInt();
        scanner.nextLine(); 
        String sourceCity = sourceOptions.get(sourceChoice - 1);

        List<Flight> matchingFlights = new ArrayList<>();
        current = head;
        System.out.println("Flights from " + sourceCity + " to " + destinationCity + ":");
        while (current != null) {
            if (current.flight.source.equalsIgnoreCase(sourceCity) &&
                current.flight.destination.equalsIgnoreCase(destinationCity)) {
                matchingFlights.add(current.flight);
                System.out.println(matchingFlights.size() + ". Departure: " + current.flight.departureTime +
                    " | Available Seats: " + current.flight.availableSeats +
                    " | Price: ₹" + current.flight.basePrice);
            }
            current = current.next;
        }

        if (matchingFlights.isEmpty()) {
            System.out.println("No flights found from " + sourceCity + " to " + destinationCity);
            return;
        }

        System.out.print("Select flight to book (enter the number): ");
        int flightChoice = scanner.nextInt();
        scanner.nextLine(); 

        Flight selectedFlight = matchingFlights.get(flightChoice - 1);

        System.out.print("Enter number of tickets: ");
        int numTickets = scanner.nextInt();
        scanner.nextLine(); 

        if (numTickets > selectedFlight.availableSeats) {
            System.out.println("Not enough seats available. Only " + selectedFlight.availableSeats + " seats are available.");
            return;
        }

        int adults = 0, children = 0, infants = 0, booked = 0;
        double totalCost = 0.0;

        for (int i = 1; i <= numTickets; i++) {
            System.out.print("Enter name for passenger " + i + ": ");
            String name = scanner.nextLine();
            System.out.print("Enter ID proof for passenger " + i + " (e.g., Passport/ID card number): ");
            String idProof = scanner.nextLine();
            System.out.print("Enter age for passenger " + i + ": ");
            int age = scanner.nextInt();
            scanner.nextLine(); 
            double price = selectedFlight.getPrice(age);
            if (age <= 2) {
                infants++;
                System.out.println("Infant (0-2) — ticket not required.");
            } else {
                String passengerLabel = name + " (Passenger " + i + ", Age " + age + ")";
                int result = selectedFlight.bookSeat(passengerLabel, 1);
                if (result == 1) {
                    userBookings.computeIfAbsent(username, k -> new ArrayList<>()).add(new BookingRecord(selectedFlight, passengerLabel));
                    saveUserBookings(username);
                    booked++;
                    totalCost += price;
                    if (age <= 12) children++;
                    else adults++;
                } else {
                    System.out.println("Flight full. " + passengerLabel + " added to waitlist.");
                }
            }
        }

        System.out.println("Booking Summary:");
        System.out.println("Adults booked: " + adults);
        System.out.println("Children booked: " + children);
        System.out.println("Infants (free): " + infants);
        System.out.println("Total booked: " + booked);
        System.out.println("Total cost: ₹" + totalCost);
    }

    // Save bookings for a user to a file and append to global bookings.txt
    public void saveUserBookings(String username) {
        List<BookingRecord> records = userBookings.get(username);
        if (records != null) {
            Set<String> existingLines = new HashSet<>();
            java.io.File userFile = new java.io.File(username + "_bookings.txt");
            // Read existing lines to avoid duplicates
            if (userFile.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(userFile))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        existingLines.add(line.trim());
                    }
                } catch (IOException e) {
                    System.out.println("Error reading existing user bookings: " + e.getMessage());
                }
            }
            // Append only new records
            try (FileWriter userWriter = new FileWriter(userFile, true)) {
                for (BookingRecord record : records) {
                    String line = record.passengerName + "," + record.flight.flightNumber;
                    if (!existingLines.contains(line)) {
                        userWriter.write(line + "\n");
                    }
                }
            } catch (IOException e) {
                System.out.println("Error saving user bookings: " + e.getMessage());
            }

            // Always append to bookings.txt for global records (as before)
            try (FileWriter allBookingsWriter = new FileWriter("bookings.txt", true)) {
                for (BookingRecord record : records) {
                    String globalLine = "User: " + username + " | Passenger: " + record.passengerName +
                        " | Flight: " + record.flight.flightNumber +
                        " | Route: " + record.flight.source + " -> " + record.flight.destination +
                        " | Departure: " + record.flight.departureTime;
                    allBookingsWriter.write(globalLine + "\n");
                }
            } catch (IOException e) {
                System.out.println("Error updating bookings.txt: " + e.getMessage());
            }
        }
    }
    // View all bookings (admin)
    public void viewAllBookings() {
        try (BufferedReader reader = new BufferedReader(new FileReader("bookings.txt"))) {
            String line;
            System.out.println("\n--- All Bookings ---");
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            System.out.println("No bookings have been made yet or unable to read file.");
        }
    }

    // Load bookings for a user from a file, matching flights from allFlights
    public void loadUserBookings(String username, List<Flight> allFlights) {
        try (BufferedReader reader = new BufferedReader(new FileReader(username + "_bookings.txt"))) {
            String line;
            List<BookingRecord> records = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    String passengerName = parts[0].trim();
                    String flightNumber = parts[1].trim();
                    for (Flight f : allFlights) {
                        if (f.flightNumber.equals(flightNumber)) {
                            records.add(new BookingRecord(f, passengerName));
                            break;
                        }
                    }
                }
            }
            userBookings.put(username, records);
        } catch (IOException e) {
            // No saved bookings; do nothing
        }
    }

    public void viewMyBookings(String username) {
        List<BookingRecord> records = userBookings.get(username);
        if (records == null || records.isEmpty()) {
            System.out.println("No bookings found.");
            return;
        }

        System.out.println("Your Bookings:");
        for (BookingRecord record : records) {
            System.out.println("Passenger: " + record.passengerName +
                " | Flight: " + record.flight.flightNumber +
                " | Route: " + record.flight.source + " -> " + record.flight.destination +
                " | Departure: " + record.flight.departureTime);
        }
    }

    public void cancelSpecificBooking(String username) {
        List<BookingRecord> records = userBookings.get(username);
        if (records == null || records.isEmpty()) {
            System.out.println("No bookings to cancel.");
            return;
        }

        System.out.println("Select a booking to cancel:");
        for (int i = 0; i < records.size(); i++) {
            BookingRecord record = records.get(i);
            System.out.println((i + 1) + ". Passenger: " + record.passengerName +
                " | Flight: " + record.flight.flightNumber +
                " | Route: " + record.flight.source + " -> " + record.flight.destination +
                " | Departure: " + record.flight.departureTime);
        }

        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the number of the booking to cancel: ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        if (choice < 1 || choice > records.size()) {
            System.out.println("Invalid choice.");
            return;
        }

        BookingRecord record = records.remove(choice - 1);
        record.flight.cancelSeat();
        System.out.println("Booking canceled for: " + record.passengerName);
        saveUserBookings(username);
    }
}

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        BookingSystem system = new BookingSystem();

        // Registration prompt before reading users file
        System.out.print("Do you have an account? (yes/no): ");
        String hasAccount = scanner.nextLine().trim().toLowerCase();

        if (hasAccount.equals("no")) {
            System.out.print("Enter new username: ");
            String newUsername = scanner.nextLine().trim();
            System.out.print("Enter new password: ");
            String newPassword = scanner.nextLine().trim();
            try (FileWriter userWriter = new FileWriter("users.txt", true)) {
                userWriter.write(newUsername + "," + newPassword + "\n");
                System.out.println("Account created successfully. You can now log in.");
            } catch (IOException e) {
                System.out.println("Error creating account: " + e.getMessage());
                return;
            }
        }

        Map<String, String> validUsers = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader("users.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    validUsers.put(parts[0].trim(), parts[1].trim());
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading users file: " + e.getMessage());
            return;
        }

        System.out.println("Welcome to the Flight Booking System");
        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Enter password: ");
        String password = scanner.nextLine().trim();

        if (!validUsers.containsKey(username) || !validUsers.get(username).equals(password)) {
            System.out.println("Invalid credentials. Exiting system.");
            return;
        }

        System.out.println("Login successful. Welcome, " + username + "!");
        try (FileWriter logWriter = new FileWriter("login_log.txt", true)) {
            logWriter.write("User '" + username + "' logged in successfully at " + new Date() + "\n");
        } catch (IOException e) {
            System.out.println("Error logging login information: " + e.getMessage());
        }

        system.addFlight(new Flight("AI101", "Delhi", "Mumbai", 5, 5000, "08:00 AM"));
        system.addFlight(new Flight("AI102", "Bangalore", "Chennai", 4, 4000, "09:30 AM"));
        system.addFlight(new Flight("AI103", "Kolkata", "Delhi", 3, 4800, "10:45 AM"));
        system.addFlight(new Flight("AI104", "Hyderabad", "Ahmedabad", 5, 4200, "01:00 PM"));
        system.addFlight(new Flight("AI229", "Delhi", "Ahmedabad", 4, 4300, "08:40 AM"));
        system.addFlight(new Flight("AI105", "Jaipur", "Goa", 6, 4500, "02:15 PM"));
        system.addFlight(new Flight("AI201", "Delhi", "Dubai", 5, 15000, "06:00 AM"));
        system.addFlight(new Flight("AI202", "Mumbai", "Singapore", 4, 18000, "11:30 AM"));
        system.addFlight(new Flight("AI203", "Chennai", "London", 3, 32000, "07:00 PM"));
        system.addFlight(new Flight("AI204", "Kolkata", "Bangkok", 6, 17000, "04:00 PM"));
        system.addFlight(new Flight("AI205", "Hyderabad", "New York", 2, 45000, "10:00 PM"));
        system.addFlight(new Flight("AI206", "Bangalore", "Paris", 3, 35000, "03:30 PM"));
        system.addFlight(new Flight("AI207", "Delhi", "Tokyo", 3, 37000, "12:00 PM"));
        system.addFlight(new Flight("AI208", "Mumbai", "Toronto", 2, 39000, "08:00 PM"));

        system.addFlight(new Flight("AI106", "Chennai", "Mumbai", 6, 4900, "05:25 AM"));
        system.addFlight(new Flight("AI107", "Goa", "Mumbai", 4, 4700, "06:50 PM"));
        system.addFlight(new Flight("AI108", "Hyderabad", "Mumbai", 5, 5000, "09:10 AM"));

        system.addFlight(new Flight("AI109", "Delhi", "Chennai", 6, 4600, "07:30 AM"));
        system.addFlight(new Flight("AI110", "Mumbai", "Chennai", 4, 4700, "02:40 PM"));
        system.addFlight(new Flight("AI111", "Kolkata", "Chennai", 5, 4800, "11:15 AM"));

        system.addFlight(new Flight("AI112", "Ahmedabad", "Delhi", 6, 4400, "06:35 AM"));
        system.addFlight(new Flight("AI113", "Bangalore", "Delhi", 4, 4900, "04:45 PM"));
        system.addFlight(new Flight("AI114", "Goa", "Delhi", 5, 5100, "08:25 PM"));

        system.addFlight(new Flight("AI209", "Bangalore", "Dubai", 4, 16000, "05:55 AM"));
        system.addFlight(new Flight("AI210", "Hyderabad", "Dubai", 3, 16500, "09:20 PM"));

        system.addFlight(new Flight("AI211", "Delhi", "Singapore", 4, 17000, "01:50 PM"));
        system.addFlight(new Flight("AI212", "Kolkata", "Singapore", 3, 17200, "07:10 AM"));

        system.addFlight(new Flight("AI213", "Mumbai", "London", 3, 33000, "10:30 AM"));
        system.addFlight(new Flight("AI214", "Hyderabad", "London", 2, 32500, "03:00 PM"));

        system.addFlight(new Flight("AI215", "Delhi", "New York", 2, 45500, "11:45 PM"));
        system.addFlight(new Flight("AI216", "Kolkata", "New York", 3, 46000, "07:35 AM"));

        system.addFlight(new Flight("AI217", "Delhi", "Goa", 4, 4600, "06:10 AM"));
        system.addFlight(new Flight("AI218", "Chandigarh", "Goa", 3, 4800, "12:30 PM"));
        system.addFlight(new Flight("AI219", "Jaipur", "Goa", 5, 4700, "07:45 PM"));

        system.addFlight(new Flight("AI230", "Mumbai", "Chandigarh", 3, 4500, "06:45 AM"));

        system.addFlight(new Flight("AI220", "Chandigarh", "Singapore", 3, 17500, "09:30 AM"));
        system.addFlight(new Flight("AI221", "Jaipur", "Singapore", 2, 17800, "06:45 PM"));
        system.addFlight(new Flight("AI222", "Delhi", "Singapore", 2, 17200, "11:15 PM"));

        system.addFlight(new Flight("AI223", "Bangalore", "Goa", 4, 4700, "01:20 PM"));
        system.addFlight(new Flight("AI224", "Hyderabad", "Goa", 3, 4600, "03:40 PM"));
        system.addFlight(new Flight("AI225", "Kolkata", "Goa", 5, 4800, "05:15 PM"));

        system.addFlight(new Flight("AI226", "Bangalore", "Singapore", 3, 17400, "07:00 AM"));
        system.addFlight(new Flight("AI227", "Ahmedabad", "Singapore", 2, 17900, "06:10 AM"));
        system.addFlight(new Flight("AI228", "Chennai", "Singapore", 3, 17100, "10:50 PM"));

        // Prepare allFlights list for loading user bookings
        List<Flight> allFlights = new ArrayList<>();
        FlightNode current = system.head;
        while (current != null) {
            allFlights.add(current.flight);
            current = current.next;
        }
        system.loadUserBookings(username, allFlights);

        while (true) {
            System.out.println("\n--- Flight Booking System ---");
            System.out.println("1. View Domestic Flights");
            System.out.println("2. View International Flights");
            System.out.println("3. View My Booking");
            System.out.println("4. Cancel Last Booking");
            System.out.println("5. Exit");
            if (username.equals("admin")) {
                System.out.println("6. View All Bookings (Admin)");
            }
            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            // Admin hidden option
            if (username.equals("admin") && choice == 6) {
                system.viewAllBookings();
                continue;
            }

            switch (choice) {
                case 1:
                    System.out.println("\n--- Domestic Flights ---");
                    system.displayFlightsByType(false, username); 
                    break;
                case 2:
                    System.out.println("\n--- International Flights ---");
                    system.displayFlightsByType(true, username); 
                    break;
                case 3:
                    system.viewMyBookings(username);
                    break;
                case 4:
                    system.cancelSpecificBooking(username);
                    break;
                case 5:
                    System.out.println("Thank you for using the system!");
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }
}