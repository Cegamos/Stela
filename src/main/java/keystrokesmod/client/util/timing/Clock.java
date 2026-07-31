package keystrokesmod.client.util.timing;

public class Clock {
    private long start;
    private long lasts;
    
    public Clock(final long lasts) {
        this.lasts = lasts;
    }
    
    public void start() {
        this.start = System.currentTimeMillis();
    }
    
    public boolean hasFinished() {
        return System.currentTimeMillis() >= this.start + this.lasts;
    }
    
	public boolean finished(long delay) {
		return System.currentTimeMillis() - delay >= start;
	}
    
    public void setCooldown(final long time) {
        this.lasts = time;
    }
    
    public long getElapsedTime() {
        return System.currentTimeMillis() - this.start;
    }
    
    public long getTimeLeft() {
        return this.lasts - (System.currentTimeMillis() - this.start);
    }
}
