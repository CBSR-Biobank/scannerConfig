public class TriIntC {
	private int a, b, c;

	public TriIntC() {
		this.init();
	}

	public void init() {
		this.a = 0;
		this.b = 0;
		this.c = 0;
	}

	private boolean validBit(int bit) {
		return (bit >= 0 && bit < 96);
	}

	public void setBit(int bit) {
		if (validBit(bit)) {
			if (bit < 32) {
				this.a |= 1 << bit;

			} else if (bit < 64) {
				this.b |= 1 << (bit - 32);

			} else { // bit < 96
				this.c |= 1 << (bit - 64);
			}
		}
	}

	public void resetBit(int bit) {
		if (validBit(bit)) {
			if (bit < 32) {
				this.a &= ~(1 << bit);

			} else if (bit < 64) {
				this.b &= ~(1 << (bit - 32));

			} else { // bit < 96
				this.c &= ~(1 << (bit - 64));
			}
		}
	}

	public boolean isSetBit(int bit) {
		if (validBit(bit)) {
			if (bit < 32) {
				return ((this.a & (1 << bit)) != 0);

			} else if (bit < 64) {
				return ((this.b & (1 << (bit - 32))) != 0);

			} else { // bit < 96
				return ((this.c & (1 << (bit - 64))) != 0);
			}
		}
		return false;
	}

	public void printBinary() {
		System.out.println("A: " + Integer.toBinaryString(this.a));
		System.out.println("B: " + Integer.toBinaryString(this.b));
		System.out.println("C: " + Integer.toBinaryString(this.c));
	}

}