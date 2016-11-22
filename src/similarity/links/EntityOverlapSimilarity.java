package similarity.links;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import model.BinaryComparison;
import model.Sentence;
import model.links.DbPediaLink;
import model.links.InternalLink;
import similarity.FeatureSimilarity;
import similarity.SimilarityType;
import translate.Language;
import util.TextUtil;

public class EntityOverlapSimilarity extends FeatureSimilarity {

	public String getName() {
		return "EntityOverlapSimilarity";
	}

	public String getEasyName() {
		return "Entity Overlap";
	}

	public SimilarityType getSimilarityType() {
		return SimilarityType.EntityOverlapSimilarity;
	}

	public double calculateSimilarity(BinaryComparison comparison, Sentence annotation1, Sentence annotation2) {

		Language commonLanguage = annotation1.getLanguage();

		List<String> links1 = this.buildLinks(annotation1, commonLanguage);
		List<String> links2 = this.buildLinks(annotation2, commonLanguage);

		if (links1.isEmpty() && links2.isEmpty()) {
			this.isApplicable = false;
			return 0.0;
		}

		return TextUtil.getJaccardSimilarity(links1, links2);
	}

	private List<String> buildLinks(Sentence sentence, Language commonLanguage) {

		Set<String> entities = new HashSet<String>();

		for (DbPediaLink link2 : sentence.getDbPediaLinks()) {
			if (link2.getEntity().getName(commonLanguage) == null)
				continue;
			entities.add(link2.getEntity().getName(commonLanguage));
		}

		for (InternalLink link : sentence.getInternalLinks()) {
			if (link.getEntity().getName(commonLanguage) == null)
				continue;
			entities.add(link.getEntity().getName(commonLanguage));
		}

		ArrayList<String> entityNames = new ArrayList<String>();
		entityNames.addAll(entities);

		return entityNames;
	}

}
