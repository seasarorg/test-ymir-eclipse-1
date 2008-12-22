package org.seasar.ymir.vili;

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;

public interface IConfigurator {
    /**
     * このインスタンスの利用を開始します。
     * <p>このメソッドはこのインスタンスが生成された直後に呼び出されます。
     * </p>
     * <p>フラグメント固有のパラメータをカスタマイズしたい場合は、
     * behaviorからpropertiesを取り出して値をセットするようにして下さい。
     * </p>
     * 
     * @param project プロジェクト。
     * このインスタンスが新規Viliプロジェクトウィザードによって生成された場合はnullが渡されます。
     * フラグメントの追加ウィザードによって生成された場合は、
     * 選択されたプロジェクトを表すIProjectインスタンスが渡されます。
     * @param behavior ViliBehaviorインスタンス。
     * @param preferences ViliProjectPreferencesインスタンス。
     */
    void start(IProject project, ViliBehavior behavior,
            ViliProjectPreferences preferences);

    /**
     * 追加のパラメータを返します。
     * <p>フラグメントを展開する際に使用される、追加のパラメータを保持するMapを生成して返します。
     * </p>
     * 
     * @param project フラグメントが追加されるプロジェクトを表すIProjectインスタンス。
     * @param behavior ViliBehaviorインスタンス。
     * @param preferences ViliProjectPreferencesインスタンス。
     * @param artifactParameters ユーザによって指定された、フラグメント固有のパラメータ。
     * @return フラグメントを展開する際に使用される、追加のパラメータを保持するMap。
     * nullを返すこともできます。
     */
    Map<String, Object> createAdditionalParameters(IProject project,
            ViliBehavior behavior, ViliProjectPreferences preferences,
            Map<String, Object> artifactParameters);

    /**
     * フラグメントの展開処理の直前に呼び出されます。
     * 
     * @param project フラグメントが追加されるプロジェクトを表すIProjectインスタンス。
     * @param behavior ViliBehaviorインスタンス。
     * @param preferences ViliProjectPreferencesインスタンス。
     * @param parameters フラグメントの展開時に使用されるパラメータ。
     * @param monitor プログレスモニタ。
     */
    void processBeforeExpanding(IProject project, ViliBehavior behavior,
            ViliProjectPreferences preferences, Map<String, Object> parameters,
            IProgressMonitor monitor);

    /**
     * 指定されたパスのリソースを展開するかどうかを返します。
     * <p>展開する場合は{@link InclusionType#INCLUDED}を、
     * 展開しない場合は{@link InclusionType#EXCLUDED}を、
     * 展開するかどうかをここでは決定しない場合は{@link InclusionType#UNDEFINED}
     * を返すようにして下さい。
     * </p>
     * <p>このメソッドの返り値はbehavior.propertiesでの
     * expansion.includes指定やexpansion.excludes指定よりも優先されます。
     * </p>
     * 
     * @param path リソースのパス（例：dbflute_${projectName}）。
     * @param resolvedPath リソースのパスを評価したパス（例：dbflute_abc）。
     * @param project フラグメントが追加されるプロジェクトを表すIProjectインスタンス。
     * @param behavior ViliBehaviorインスタンス。
     * @param preferences ViliProjectPreferencesインスタンス。
     * @param parameters フラグメントの展開時に使用されたパラメータ。
     * @return リソースを展開するかどうか。
     */
    InclusionType shouldExpand(String path, String resolvedPath,
            IProject project, ViliBehavior behavior,
            ViliProjectPreferences preferences, Map<String, Object> parameters);

    /**
     * フラグメントの展開処理の直後に呼び出されます。
     * 
     * @param project フラグメントが追加されるプロジェクトを表すIProjectインスタンス。
     * @param behavior ViliBehaviorインスタンス。
     * @param preferences ViliProjectPreferencesインスタンス。
     * @param parameters フラグメントの展開時に使用されたパラメータ。
     * @param monitor プログレスモニタ。
     */
    void processAfterExpanded(IProject project, ViliBehavior behavior,
            ViliProjectPreferences preferences, Map<String, Object> parameters,
            IProgressMonitor monitor);
}
