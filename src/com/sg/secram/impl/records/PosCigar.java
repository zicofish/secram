package com.sg.secram.impl.records;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * Maintain the base information of a position compared to that on the reference sequence.
 * For example, if reference base on this position is 'A', while the base of a read on this 
 * position is 'C', then a feature that indicates such substitution information is maintained
 * by an instance of this class for the position.
 * <p>
 * Therefore, PosCigar keeps track of such "difference" information w.r.t the reference for all
 * the reads that overlap this position.
 * <p>
 * See also:
 * <ul>
 * <li>{@link PosCigarFeature} for definition of a feature.</li>
 * <li>{@link PosCigarFeatureCode} for the list of defined features.</li>
 * </ul>
 * @author zhihuang
 *
 */
public class PosCigar {

	/** Number of reads that overlap this position */
	public int mCoverage = 0;

	private Map<Integer, List<PosCigarFeature>> mReadFeaturesMap = new TreeMap<>();
	private char mRefBase;

	/**
	 * Construct by specifying a reference base.
	 */
	public PosCigar(char refBase) {
		mRefBase = refBase;
	}

	public void setReferenceBase(char base) {
		mRefBase = base;
	}

	/**
	 * Get the differences w.r.t the reference on this position.
	 */
	public List<PosCigarFeature> getNonMatchFeatures() {
		LinkedList<PosCigarFeature> posCigarFeatures = new LinkedList<PosCigarFeature>();
		for (Entry<Integer, List<PosCigarFeature>> entry : mReadFeaturesMap
				.entrySet()) {
			posCigarFeatures.addAll(entry.getValue());
		}
		return posCigarFeatures;
	}

	/**
	 * Set features for the read with the specified order on this position.
	 */
	public void setNonMatchFeaturesForRead(int order,
			List<PosCigarFeature> features) {
		if (order >= mCoverage)
			throw new IndexOutOfBoundsException("The coverage is only: "
					+ mCoverage + ", but order " + order + " is given");
		mReadFeaturesMap.put(order, features);
	}

	public List<PosCigarFeature> getNonMatchFeaturesOfRead(int order) {
		if (order >= mCoverage)
			throw new IndexOutOfBoundsException("The coverage is only: "
					+ mCoverage + ", but order " + order + " is given");
		return mReadFeaturesMap.get(order);
	}

	/**
	 * The complete features of the read, including reference match if there is
	 * one.
	 */
	public List<PosCigarFeature> getCompleteFeaturesOfRead(int order) {
		if (order >= mCoverage)
			throw new IndexOutOfBoundsException("The coverage is only: "
					+ mCoverage + ", but order " + order + " is given");
		List<PosCigarFeature> features = mReadFeaturesMap.get(order);
		if (null == features) {
			features = new LinkedList<PosCigarFeature>();
			features.add(new PosCigarFeature(order, 'M', 1, String
					.valueOf(mRefBase)));
			return features;
		}
		List<PosCigarFeature> completeFeatures = new LinkedList<PosCigarFeature>();
		completeFeatures.addAll(features);
		boolean addMatchOP = true, breakloop = false;
		int i;
		for (i = 0; i < completeFeatures.size(); i++) {
			PosCigarFeature tmp = completeFeatures.get(i);
			switch (tmp.mOP) {
			case X:
			case D:
			case N:
			case M:
				addMatchOP = false;
				breakloop = true;
				break;
			case I:
			case S:
			case H:
			case P:
				breakloop = true;
				break;
			default:
			}
			if (breakloop)
				break;
		}
		if (addMatchOP) {
			completeFeatures
					.add(i,
							new PosCigarFeature(order, 'M', 1, String
									.valueOf(mRefBase)));
		}
		return completeFeatures;
	}

	/**
	 * @return The number of bases on this position. This is different from
	 *         coverage, because of insertion, deletion, etc.
	 */
	public int getNumberOfBases() {
		int nb = 0;
		for (int i = 0; i < mCoverage; i++) {
			List<PosCigarFeature> features = getCompleteFeaturesOfRead(i);
			for (PosCigarFeature f : features) {
				nb += f.mBases.length();
			}
		}
		return nb;
	}

	@Override
	public String toString() {
		String result = "";
		List<PosCigarFeature> list = getNonMatchFeatures();
		for (PosCigarFeature element : list) {
			result += element + " ";
		}
		return result;
	}

}
