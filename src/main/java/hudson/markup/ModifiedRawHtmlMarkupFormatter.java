package hudson.markup;

import hudson.Extension;

import java.io.IOException;
import java.io.Writer;

import org.kohsuke.stapler.DataBoundConstructor;
import org.owasp.html.Handler;
import org.owasp.html.HtmlSanitizer;
import org.owasp.html.HtmlStreamRenderer;

import com.google.common.base.Throwables;

/**
 * {@link MarkupFormatter} that treats the input as the raw html.
 * This is the backward compatible behaviour.
 *
 * @author Kohsuke Kawaguchi
 */
public class ModifiedRawHtmlMarkupFormatter extends MarkupFormatter {

    final boolean disableSyntaxHighlighting;

    @DataBoundConstructor
    public ModifiedRawHtmlMarkupFormatter(final boolean disableSyntaxHighlighting) {
        this.disableSyntaxHighlighting = disableSyntaxHighlighting;
    }

    public boolean isDisableSyntaxHighlighting() {
        return disableSyntaxHighlighting;
    }

    @Override
    public void translate(String markup, Writer output) throws IOException {
        HtmlStreamRenderer renderer = HtmlStreamRenderer.create(
                output,
                // Receives notifications on a failure to write to the output.
                new Handler<IOException>() {
                    public void handle(IOException ex) {
                        Throwables.propagate(ex);  // System.out suppresses IOExceptions
                    }
                },
                // Our HTML parser is very lenient, but this receives notifications on
                // truly bizarre inputs.
                new Handler<String>() {
                    public void handle(String x) {
                        throw new Error(x);
                    }
                }
        );
        // Use the policy defined above to sanitize the HTML.
        HtmlSanitizer.sanitize(markup, ModifiedMyspacePolicy.POLICY_DEFINITION.apply(renderer));
    }

    public String getCodeMirrorMode() {
        return disableSyntaxHighlighting ? null : "htmlmixed";
    }

    public String getCodeMirrorConfig() {
        return "mode:'text/html'";
    }

    @Extension
    public static class DescriptorImpl extends MarkupFormatterDescriptor {
        @Override
        public String getDisplayName() {
            return "Raw HTML (modified)";
        }
    }

    public static final MarkupFormatter INSTANCE = new ModifiedRawHtmlMarkupFormatter(false);
}
