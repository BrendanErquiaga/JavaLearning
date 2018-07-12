/**
 * Created by berquiaga on 6/22/17.
 */
public class EnumsTest {

    public static enum GlobalTypeCode {
        ARTICLE(SourceType.ARTICLE);

        public SourceType getSourceType() {
            return sourceType;
        }

        private SourceType sourceType;
        GlobalTypeCode(SourceType sourceType){
            this.sourceType = sourceType;
        }
    }

    public static enum SourceType {
        MEDIA,
        ARTICLE,
        DATA;
    }
}
