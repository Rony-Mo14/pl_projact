package healthclub.ui;

import java.util.Scanner;
import java.util.ArrayList;
import healthclub.filehandler.FileManager;
import healthclub.model.*;
import healthclub.service.AuthService;

public class Menu {

    private Scanner input = new Scanner(System.in);
    private FileManager fileManager = new FileManager();
    private AuthService authService = new AuthService();

    public void showMenu() {

        System.out.println("===== Welcome to Health Club =====");
        System.out.print("Enter Email: ");
        String email = input.nextLine();
        System.out.print("Enter Password: ");
        String password = input.nextLine();

        String role = authService.login(email, password);

        if (role == null) {
            System.out.println("Invalid email or password!");
            return;
        }

        switch (role) {
            case "admin":
                showAdminMenu();
                break;
            case "coach":
                Coach coach = authService.getCoachByEmail(email);
                showCoachMenu(coach);
                break;
            case "member":
                Member member = authService.getMemberByEmail(email);
                showMemberMenu(member);
                break;
        }
    }

    // ==================== ADMIN MENU ====================

    private void showAdminMenu() {
        int choice;
        do {
            System.out.println("\n===== Admin Menu =====");
            System.out.println("1. Add Member");
            System.out.println("2. View Members");
            System.out.println("3. Search Member by Name");
            System.out.println("4. Update Member");
            System.out.println("5. Delete Member");
            System.out.println("6. Add Coach");
            System.out.println("7. View Coaches");
            System.out.println("8. Search Coach by Name");
            System.out.println("9. Update Coach");
            System.out.println("10. Delete Coach");
            System.out.println("11. View Billing");
            System.out.println("12. Check Expiring Subscriptions");
            System.out.println("13. Exit");
            System.out.print("Enter your choice: ");
            choice = input.nextInt();
            input.nextLine();

            switch (choice) {
                case 1:
                    int memberId = fileManager.generateMemberId();
                    System.out.print("Enter Name: ");
                    String name = input.nextLine();
                    System.out.print("Enter Email: ");
                    String email = input.nextLine();
                    System.out.print("Enter Password: ");
                    String password = input.nextLine();
                    System.out.print("Enter Phone: ");
                    String phone = input.nextLine();
                    System.out.print("Enter Age: ");
                    int age = input.nextInt();
                    input.nextLine();
                    System.out.print("Enter Subscription End Date (yyyy-MM-dd): ");
                    String endDate = input.nextLine();
                    System.out.print("Enter Coach ID: ");
                    int coachId = input.nextInt();
                    input.nextLine();
                    Member newMember = new Member(memberId, name, email, password,
                            phone, age, endDate, coachId);
                    fileManager.addMember(newMember);
                    System.out.println("Member added! ID: " + memberId);
                    break;

                case 2:
                    ArrayList<Member> members = fileManager.getAllMembers();
                    if (members.isEmpty()) {
                        System.out.println("No members found.");
                    } else {
                        for (Member m : members) {
                            System.out.println("ID: " + m.getId() +
                                " | Name: " + m.getName() +
                                " | Email: " + m.getEmail() +
                                " | Phone: " + m.getPhone() +
                                " | Subscription End: " + m.getSubscriptionEndDate() +
                                " | Coach ID: " + m.getCoachId());
                        }
                    }
                    break;

                case 3:
                    System.out.print("Enter Name to search: ");
                    String searchName = input.nextLine();
                   ArrayList<Member> found =
        fileManager.searchMembers(searchName);
                    if (found.isEmpty()) {
                        System.out.println("No members found.");
                    } else {
                        for (Member m : found) {
                            System.out.println("ID: " + m.getId() +
                                " | Name: " + m.getName() +
                                " | Email: " + m.getEmail());
                        }
                    }
                    break;

                case 4:
                    System.out.print("Enter Member ID to update: ");
                    int updateId = input.nextInt();
                    input.nextLine();
                    Member existing = fileManager.getMemberById(updateId);
                    if (existing == null) {
                        System.out.println("Member not found.");
                        break;
                    }
                    System.out.print("Enter New Name: ");
                    existing.setName(input.nextLine());
                    System.out.print("Enter New Phone: ");
                    existing.setPhone(input.nextLine());
                    System.out.print("Enter New Subscription End Date (yyyy-MM-dd): ");
                    existing.setSubscriptionEndDate(input.nextLine());
                    System.out.print("Enter New Coach ID: ");
                    existing.setCoachId(input.nextInt());
                    input.nextLine();
                    fileManager.updateMember(existing);
                    System.out.println("Member updated!");
                    break;

                case 5:
                    System.out.print("Enter Member ID to delete: ");
                    int deleteId = input.nextInt();
                    input.nextLine();
                    fileManager.deleteMember(deleteId);
                    break;

                case 6:
                    int coachNewId = fileManager.generateCoachId();
                    System.out.print("Enter Coach Name: ");
                    String cName = input.nextLine();
                    System.out.print("Enter Coach Email: ");
                    String cEmail = input.nextLine();
                    System.out.print("Enter Coach Password: ");
                    String cPassword = input.nextLine();
                    System.out.print("Enter Coach Phone: ");
                    String cPhone = input.nextLine();
                    System.out.print("Enter Coach Age: ");
                    int cAge = input.nextInt();
                    input.nextLine();
                    Coach newCoach = new Coach(coachNewId, cName, cEmail, cPassword,
                            cPhone, cAge, "", "");
                    fileManager.addCoach(newCoach);
                    System.out.println("Coach added! ID: " + coachNewId);
                    break;

                case 7:
                    ArrayList<Coach> coaches = fileManager.getAllCoaches();
                    if (coaches.isEmpty()) {
                        System.out.println("No coaches found.");
                    } else {
                        for (Coach c : coaches) {
                            System.out.println("ID: " + c.getId() +
                                " | Name: " + c.getName() +
                                " | Email: " + c.getEmail() +
                                " | Phone: " + c.getPhone());
                        }
                    }
                    break;

                case 8:
                    System.out.print("Enter Coach Name to search: ");
                    String cSearchName = input.nextLine();
                  ArrayList<Coach> foundCoaches =
        fileManager.searchCoaches(cSearchName);
                    if (foundCoaches.isEmpty()) {
                        System.out.println("No coaches found.");
                    } else {
                        for (Coach c : foundCoaches) {
                            System.out.println("ID: " + c.getId() +
                                " | Name: " + c.getName() +
                                " | Email: " + c.getEmail());
                        }
                    }
                    break;

                case 9:
                    System.out.print("Enter Coach ID to update: ");
                    int cUpdateId = input.nextInt();
                    input.nextLine();
                    Coach existingCoach = fileManager.getCoachById(cUpdateId);
                    if (existingCoach == null) {
                        System.out.println("Coach not found.");
                        break;
                    }
                    System.out.print("Enter New Name: ");
                    existingCoach.setName(input.nextLine());
                    System.out.print("Enter New Phone: ");
                    existingCoach.setPhone(input.nextLine());
                    fileManager.updateCoach(existingCoach);
                    System.out.println("Coach updated!");
                    break;

                case 10:
                    System.out.print("Enter Coach ID to delete: ");
                    int cDeleteId = input.nextInt();
                    input.nextLine();
                    fileManager.deleteCoach(cDeleteId);
                    break;

                case 11:
                    ArrayList<String> billing = fileManager.getAllBilling();
                    if (billing.isEmpty()) {
                        System.out.println("No billing records.");
                    } else {
                        for (String b : billing) {
                            System.out.println(b);
                        }
                    }
                    break;

                case 12:
                    ArrayList<String> notifications = fileManager.checkExpiringSubscriptions();
                    if (notifications.isEmpty()) {
                        System.out.println("No expiring subscriptions.");
                    } else {
                        for (String n : notifications) {
                            System.out.println(n);
                        }
                    }
                    break;

                case 13:
                    System.out.println("Goodbye!");
                    break;

                default:
                    System.out.println("Invalid choice");
            }

        } while (choice != 13);
    }

    // ==================== COACH MENU ====================

    private void showCoachMenu(Coach coach) {
        int choice;
        do {
            System.out.println("\n===== Coach Menu ===== Welcome " + coach.getName());
            System.out.println("1. View My Members");
            System.out.println("2. Set Plan & Schedule");
            System.out.println("3. Send Message to Members");
            System.out.println("4. Update My Info");
            System.out.println("5. Exit");
            System.out.print("Enter your choice: ");
            choice = input.nextInt();
            input.nextLine();

            switch (choice) {
                case 1:
                    ArrayList<Member> members = fileManager.getAllMembers();
                    boolean found = false;
                    for (Member m : members) {
                        if (m.getCoachId() == coach.getId()) {
                            System.out.println("ID: " + m.getId() +
                                " | Name: " + m.getName() +
                                " | Subscription End: " + m.getSubscriptionEndDate());
                            found = true;
                        }
                    }
                    if (!found) System.out.println("No members assigned to you.");
                    break;

                case 2:
                    System.out.print("Enter Plan: ");
                    coach.setPlan(input.nextLine());
                    System.out.print("Enter Schedule: ");
                    coach.setSchedule(input.nextLine());
                    fileManager.updateCoach(coach);
                    System.out.println("Plan & Schedule updated!");
                    break;

                case 3:
                    System.out.print("Enter Message: ");
                    String message = input.nextLine();
                    fileManager.sendMessageToMembers(coach.getId(), message);
                    System.out.println("Message sent!");
                    break;

                case 4:
                    System.out.print("Enter New Name: ");
                    coach.setName(input.nextLine());
                    System.out.print("Enter New Phone: ");
                    coach.setPhone(input.nextLine());
                    System.out.print("Enter New Password: ");
                    coach.setPassword(input.nextLine());
                    fileManager.updateCoach(coach);
                    System.out.println("Info updated!");
                    break;

                case 5:
                    System.out.println("Goodbye!");
                    break;

                default:
                    System.out.println("Invalid choice");
            }

        } while (choice != 5);
    }

    // ==================== MEMBER MENU ====================

    private void showMemberMenu(Member member) {
        int choice;
        do {
            System.out.println("\n===== Member Menu ===== Welcome " + member.getName());
            System.out.println("1. View My Subscription");
            System.out.println("2. View My Coach");
            System.out.println("3. View Messages from Coach");
            System.out.println("4. Update My Info");
            System.out.println("5. Exit");
            System.out.print("Enter your choice: ");
            choice = input.nextInt();
            input.nextLine();

            switch (choice) {
                case 1:
                    System.out.println("Subscription End Date: " + 
                        member.getSubscriptionEndDate());
                    break;

                case 2:
                    Coach coach = fileManager.getCoachById(member.getCoachId());
                    if (coach != null) {
                        System.out.println("Coach: " + coach.getName());
                        System.out.println("Plan: " + coach.getPlan());
                        System.out.println("Schedule: " + coach.getSchedule());
                    } else {
                        System.out.println("No coach assigned.");
                    }
                    break;

                case 3:
                    ArrayList<String> msgs = fileManager.getMessagesForMember(
                        member.getCoachId());
                    if (msgs.isEmpty()) {
                        System.out.println("No messages.");
                    } else {
                        for (String msg : msgs) {
                            System.out.println(msg);
                        }
                    }
                    break;

                case 4:
                    System.out.print("Enter New Name: ");
                    member.setName(input.nextLine());
                    System.out.print("Enter New Phone: ");
                    member.setPhone(input.nextLine());
                    System.out.print("Enter New Password: ");
                    member.setPassword(input.nextLine());
                    fileManager.updateMember(member);
                    System.out.println("Info updated!");
                    break;

                case 5:
                    System.out.println("Goodbye!");
                    break;

                default:
                    System.out.println("Invalid choice");
            }

        } while (choice != 5);
    }
}