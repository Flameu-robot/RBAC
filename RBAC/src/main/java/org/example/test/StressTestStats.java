package org.example.test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class StressTestStats {

    private final AtomicInteger usersCreated     = new AtomicInteger(0);
    private final AtomicInteger usersUpdated     = new AtomicInteger(0);
    private final AtomicInteger assignmentsCreated = new AtomicInteger(0);
    private final AtomicInteger filtersExecuted  = new AtomicInteger(0);
    private final AtomicInteger duplicatesSkipped = new AtomicInteger(0);
    private final AtomicInteger errorsCount      = new AtomicInteger(0);
    private final AtomicLong    usersFound       = new AtomicLong(0);
    private final AtomicLong    assignmentsFound = new AtomicLong(0);

    private final List<String> errors = new ArrayList<>();
    private final Object errorsLock   = new Object();

    public void recordUserCreated()       { usersCreated.incrementAndGet(); }
    public void recordUserUpdated()       { usersUpdated.incrementAndGet(); }
    public void recordAssignmentCreated() { assignmentsCreated.incrementAndGet(); }
    public void recordFilterExecuted()    { filtersExecuted.incrementAndGet(); }
    public void recordDuplicateSkipped()  { duplicatesSkipped.incrementAndGet(); }
    public void recordUsersFound(int n)   { usersFound.addAndGet(n); }
    public void recordAssignmentsFound(int n) { assignmentsFound.addAndGet(n); }

    public void recordError(String message) {
        errorsCount.incrementAndGet();
        synchronized (errorsLock) {
            if (errors.size() < 50) {
                errors.add(message);
            }
        }
    }

    public int getUsersCreated()      { return usersCreated.get(); }
    public int getUsersUpdated()      { return usersUpdated.get(); }
    public int getAssignmentsCreated() { return assignmentsCreated.get(); }
    public int getFiltersExecuted()   { return filtersExecuted.get(); }
    public int getDuplicatesSkipped() { return duplicatesSkipped.get(); }
    public int getErrorsCount()       { return errorsCount.get(); }
    public long getUsersFound()       { return usersFound.get(); }
    public long getAssignmentsFound() { return assignmentsFound.get(); }

    public List<String> getErrors() {
        synchronized (errorsLock) {
            return new ArrayList<>(errors);
        }
    }
}
