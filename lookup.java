import org.xbill.DNS.Type;
import org.xbill.DNS.Record;
import org.xbill.DNS.Name;
import org.xbill.DNS.Lookup;

// 
// Decompiled by Procyon v0.5.36
// 

public class lookup
{
    public static void printAnswer(final String name, final Lookup lookup) {
        System.out.print(name + ":");
        final int result = lookup.getResult();
        if (result != 0) {
            System.out.print(" " + lookup.getErrorString());
        }
        System.out.println();
        final Name[] aliases = lookup.getAliases();
        if (aliases.length > 0) {
            System.out.print("# aliases: ");
            for (int i = 0; i < aliases.length; ++i) {
                System.out.print(aliases[i]);
                if (i < aliases.length - 1) {
                    System.out.print(" ");
                }
            }
            System.out.println();
        }
        if (lookup.getResult() == 0) {
            final Record[] answers = lookup.getAnswers();
            for (int j = 0; j < answers.length; ++j) {
                System.out.println(answers[j]);
            }
        }
    }
    
    public static void main(final String[] args) throws Exception {
        int type = 1;
        int start = 0;
        if (args.length > 2 && args[0].equals("-t")) {
            type = Type.value(args[1]);
            if (type < 0) {
                throw new IllegalArgumentException("invalid type");
            }
            start = 2;
        }
        for (int i = start; i < args.length; ++i) {
            final Lookup l = new Lookup(args[i], type);
            l.run();
            printAnswer(args[i], l);
        }
    }
}
